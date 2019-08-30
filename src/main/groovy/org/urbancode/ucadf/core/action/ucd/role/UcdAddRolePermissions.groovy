/**
 * This action adds permissions to a role.
 */
package org.urbancode.ucadf.core.action.ucd.role

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecurityTypePermissions
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.role.UcdRole
import org.urbancode.ucadf.core.model.ucd.role.UcdRolePermissionMap

import groovy.json.JsonBuilder

class UcdAddRolePermissions extends UcAdfAction {
	/** The role. */
	String role
	
	/** The security type. */
	String type
	
	/** The security subtype. */
	String subtype
	
	/** The list of permissions. */
	List<String> permissions
	
	/** Remove other permissions. If true then all existing permissions other than the ones specified will be removed. */
	Boolean removeOthers = false

	/** The flag that indicates fail if the permission is not valid (not found) for the instance. Default is true. */
	Boolean failIfNotFound = true
		
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		// Get the role information.
		UcdRole ucdRole = actionsRunner.runAction([
			action: UcdGetRole.getSimpleName(),
			actionInfo: false,
			role: role
		])

		// Get the security type permissions map.
		Map<String, Map> securityTypePermissionsMapById = actionsRunner.runAction([
			action: UcdGetSecurityTypePermissions.getSimpleName(),
			actionInfo: false,
			returnAs: UcdGetSecurityTypePermissions.ReturnAsEnum.MAPBYID
		])

		UcdRolePermissionMap ucdRolePermissionMap = ucdRole.getRolePermissionMap(securityTypePermissionsMapById)
		
		// Add role permissions.
		for (permission in permissions) {
			// Determine if the permission exists for the instance.
			if (!securityTypePermissionsMapById.find { type.equals(it.value['type']) && permission.equals(it.value['permission']) }) {
				if (failIfNotFound) {
					throw new UcdInvalidValueException("Security [$type] [$permission] doesen't exist for this instance.")
				}
				
				println "Skipping [$type] [$permission] that doesen't exist for the instance."
				
				continue
			}
			
			logInfo("Adding role [$role] subtype [$subtype] permission [$permission].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/role/{role}/actionMappings")
				.resolveTemplate("role", role)
			logDebug("target=$target".toString())
			
			Map<String, Map<String, String>> securityTypeActionsMapByName = actionsRunner.runAction([
				action: UcdGetSecurityTypePermissions.getSimpleName(),
				actionInfo: false,
				returnAs: UcdGetSecurityTypePermissions.ReturnAsEnum.MAPBYNAME
			])
			String permissionId = securityTypeActionsMapByName[type][permission]
	
			// Construct the request map.
			Map requestMap = [:]
			if (subtype) {
				requestMap.put("resourceRole", subtype)
				requestMap.put("action", permissionId)
			} else {
				requestMap.put("action", permissionId)
			}
	
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

			Response response = target.request().post(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() != 200) {
				throw new UcdInvalidValueException(response)
			}
		}
		
		// Remove extra role permissions.
		if (removeOthers) {
			logInfo("Removing extra role permissions.")
			for (persistedPermission in ucdRolePermissionMap.getSubtypePermissions(type, subtype)) {
				if (!permissions.find { it == persistedPermission }) {
					// Remove the permission.
					actionsRunner.runAction([
						action: UcdRemoveRolePermissions.getSimpleName(),
						role: role,
						type: type,
						subtype: subtype,
						permission: persistedPermission
					])
				}
			}
		}
	}
}
