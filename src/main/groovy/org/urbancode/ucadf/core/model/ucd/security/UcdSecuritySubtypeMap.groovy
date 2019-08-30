/**
 * Not a UCD entity but rather a way to set up a key/role map for repeated lookup for a set of security types (resource roles)
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnore

class UcdSecuritySubtypeMap extends UcdObject {
	public final static String KEY_ID = "id"
	public final static String KEY_NAME = "name"
	
	@Delegate Map<String, UcdSecuritySubtype> resourceRolesMap = new TreeMap()
	
	public UcdSecuritySubtype put(String keyName, UcdSecuritySubtype resRole) {
		resourceRolesMap[keyName] = resRole
		return resRole
	}
	
	@JsonIgnore
	public UcdSecuritySubtype get(String keyName) {
		return resourceRolesMap[keyName]
	}
		
	@JsonIgnore
	public Set keySet() {
		return resourceRolesMap.keySet()
	}
}
