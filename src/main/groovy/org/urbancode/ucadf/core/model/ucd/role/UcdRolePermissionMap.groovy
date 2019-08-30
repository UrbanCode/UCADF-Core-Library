/**
 * This class instantiates role permission maps objects.
 */
package org.urbancode.ucadf.core.model.ucd.role

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypePermission

import com.fasterxml.jackson.annotation.JsonIgnore

class UcdRolePermissionMap extends UcdObject {
	// The permission map.
	private Map<String, Map<String, Map<String, UcdSecurityTypePermission>>> permissionMap = new TreeMap()
	
	public addType(final String type) {
		if (!permissionMap[type]) {
			permissionMap[type] = new TreeMap()
		}
	}
	
	public addSubtype(
		final String type, 
		final String subtype) {
		
		if (!permissionMap[type][subtype]) {
			permissionMap[type][subtype] = new TreeMap()
		}
	}
	
	public addPermission(
		final String type, 
		final String subtype, 
		final UcdSecurityTypePermission ucdSecurityTypePermission) {
		
		permissionMap[type][subtype][ucdSecurityTypePermission.getPermission().getName()] = ucdSecurityTypePermission		
	}
	
	@JsonIgnore	
	public getTypes() {
		return permissionMap.keySet()
	}

	@JsonIgnore	
	public getSubtypes(final String type)	{
		return permissionMap[type].keySet()
	}
	
	@JsonIgnore	
	public Set<String> getSubtypePermissions(
		final String type, 
		final String subtype) {
		
		Set permissions = []
		
		if (permissionMap.containsKey(type) && permissionMap[type].containsKey(subtype)) {
			permissions = permissionMap[type][subtype].keySet()
		}
		
		return permissions
	}
}
