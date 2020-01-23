/**
 * This action returns the collection of permissions available for the specified securitiy type.
 * <p>
 * The UCD APIs refer to these permissions as resource type actions.
 */
package org.urbancode.ucadf.core.action.ucd.security

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityType
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypePermission

import com.fasterxml.jackson.annotation.JsonIgnore

class UcdGetSecurityTypePermissions extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a map having the permission name as the key. */
		MAPBYNAME,
		
		/** Return as a map having the permission ID as the key. */
		MAPBYID
	}
	
	// Action properties.
	
	/** The type of collection to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.MAPBYNAME
	
	// Static so the information is only loaded once.
 	private static Map<String, Map> securityTypePermissionsMapById
	private static Map<String, Map<String, String>> securityTypePermissionsMapByName
	
	/** 
	 * Runs the action 
	 * @return Returns a collection of the requested type.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting security type permissions [$returnAs].")

		// If the maps haven't been loaded already then load them.	
		if (!securityTypePermissionsMapByName) {
			securityTypePermissionsMapByName = [:]
			securityTypePermissionsMapById = [:]
			
			// Get the list of security types.
			List<UcdSecurityType> ucdSecurityTypes = actionsRunner.runAction([
				action: UcdGetSecurityTypes.getSimpleName(),
				actionInfo: false
			])
			
			for (ucdSecurityType in ucdSecurityTypes) {
				// Get the security type permissions for the security type.
				List<UcdSecurityTypePermission> ucdSecurityTypePermissions = getSecurityTypePermissionsForType(ucdSecurityType.getName())
				
				// Process each of the security type permissions.
				for (ucdSecurityTypePermission in ucdSecurityTypePermissions) {
					// Load the security type permissions map with name key.
					if (!securityTypePermissionsMapByName.containsKey(ucdSecurityType.getName())) {
						securityTypePermissionsMapByName[ucdSecurityType.getName()] = [:]
					}
					securityTypePermissionsMapByName[ucdSecurityType.getName()][ucdSecurityTypePermission.getName()] = ucdSecurityTypePermission.getId()
					
					// Load the security type permissions map with ID key.
					securityTypePermissionsMapById[ucdSecurityTypePermission.getId()] = [
						type: ucdSecurityType.getName(), 
						permission: ucdSecurityTypePermission.getName()
					]
				}
			}
		}

		// Return as requested.
		Object securityTypePermissions
		if (returnAs.equals(ReturnAsEnum.MAPBYNAME)) {
			securityTypePermissions = securityTypePermissionsMapByName
		} else {
			securityTypePermissions = securityTypePermissionsMapById
		}
		
		return securityTypePermissions
	}

	// Get the resource type roles for a given resource type.
	@JsonIgnore
	private List<UcdSecurityTypePermission> getSecurityTypePermissionsForType(final String type) {
		List<UcdSecurityTypePermission> ucdSecurityTypepermissions = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/resourceType/{type}/actions")
			.resolveTemplate("type", type)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdSecurityTypepermissions = response.readEntity(new GenericType<List<UcdSecurityTypePermission>>(){})
		} else {
			throw new UcdInvalidValueException(response)
		}
		
		return ucdSecurityTypepermissions
	}
}
