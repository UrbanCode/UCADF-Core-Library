/**
 * This action remove role permissions.
 */
package org.urbancode.ucadf.core.action.ucd.role

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecurityTypePermissions
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

import groovy.json.JsonBuilder

class UcdRemoveRolePermissions extends UcAdfAction {
	// Action properties.
	/** The role. */
	String role
	
	/** The security type. */
	String type
	
	/** The security subtype. */
	String subtype = ""
	
	/** The list of permissions to remove. */
	List<String> permissions
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		// Add role permissions.
		for (permission in permissions) {
			logVerbose("Removing role [$role] subtype [$subtype] permission [$permission].")
			
			// Must the the non-compliant HTTP client to pass in a DELETE body.
			WebTarget target = ucdSession.getWebNonCompliantClient().target(ucdSession.getUcdUrl()).path("/security/role/{role}/actionMappings")
				.resolveTemplate("role", role)
			logDebug("target=$target".toString())
			
			Map<String, Map<String, String>> securityTypePermissionsMapByName = actionsRunner.runAction([
				action: UcdGetSecurityTypePermissions.getSimpleName(),
				actionInfo: false,
				returnAs: UcdGetSecurityTypePermissions.ReturnAsEnum.MAPBYNAME
			])
			String permissionId = securityTypePermissionsMapByName[type][permission]
	
			// Construct the request map.
			Map requestMap = [:]
			if (subtype) {
				requestMap.put("resourceRole", subtype)
				requestMap.put("action", permissionId)
			} else {
				requestMap.put("action", permissionId)
			}
	
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
			target.request().accept(MediaType.TEXT_PLAIN).method("DELETE", Entity.json(jsonBuilder.toString()))
			Response response = target.request().accept(MediaType.TEXT_PLAIN).method("DELETE", Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				logVerbose("Role [$role] subtype [$subtype] permission [$permission] removed.")
			} else {
				throw new UcAdfInvalidValueException(response)
			}
		}
	}
}
