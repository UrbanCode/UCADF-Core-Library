/**
 * This class instantiates role objects.
 */
package org.urbancode.ucadf.core.model.ucd.role

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypePermission

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class UcdRole extends UcdObject {
	/** The role ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The flag to indicate the role is deletable. */	
	Boolean isDeletable
	
	/** The role permission mappings (known to the UCD APIs as actionMappings */
	@JsonProperty("actionMappings")
	List<UcdSecurityTypePermission> permissionMappings
	
	// Constructors.
	UcdRole() {
	}
	
	/**
	 * Get action map for a role with security type name as a key, then security subtype name as a key, then permission name as a key.
	 * @param securityTypePermissionsMapById
	 * @return The role permission map.
	 */
	@JsonIgnore	
	public UcdRolePermissionMap getRolePermissionMap(final Map<String, Map> securityTypePermissionsMapById) {
		UcdRolePermissionMap permissionMap = new UcdRolePermissionMap()
		
		for (UcdSecurityTypePermission ucdSecurityTypePermission in permissionMappings) {
			String permissionId = ucdSecurityTypePermission.getPermission().getId()
			String type = securityTypePermissionsMapById[permissionId]["type"]
			String permission = securityTypePermissionsMapById[permissionId]["permission"]

			// Add the resource type to the map.
			permissionMap.addType(type)

			// Add the subtype to the map.
			String subtype
			if (ucdSecurityTypePermission.getSubtype()) {
				subtype = ucdSecurityTypePermission.getSubtype().getName()
			} else {
				subtype = ""
			}
			
			permissionMap.addSubtype(
				type, 
				subtype
			)

			// Add the action to the map.
			permissionMap.addPermission(
				type, 
				subtype, 
				ucdSecurityTypePermission
			)
		}
		
		return permissionMap
	}
}
