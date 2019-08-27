/**
 * Not a UCD entity but rather a way to set up a key/role map for repeated lookup for a set of roles.
 */
package org.urbancode.ucadf.core.model.ucd.role
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnore

class UcdRolesMap extends UcdObject {
	public final static String KEY_ID = "id"
	public final static String KEY_NAME = "name"
	
	@Delegate Map<String, UcdRole> rolesMap = new TreeMap()
	
	public UcdRole put(String roleKey, UcdRole role) {
		rolesMap[roleKey] = role
		return role
	}
	
	public UcdRole get(String roleKey) {
		return rolesMap[roleKey]
	}
		
	@JsonIgnore
	public Set keySet() {
		return rolesMap.keySet()
	}

	/**
	 * Get an optionally filtered map of roles with the specified key.
	 * @param ucdRoles The list of roles.
	 * @param keyName The key name.
	 * @param match The match regular expression.
	 * @return The roles map.
	 */
	@JsonIgnore
	public static UcdRolesMap getRolesMap(
		final List<UcdRole> ucdRoles,
		final String keyName, 
		final String match = null) {
		
		UcdRolesMap rolesMap = new UcdRolesMap()
		for (role in ucdRoles) {
			if (!match || (role[keyName] ==~ match)) {
				rolesMap.put(role[keyName], role)
			}
		}

		return rolesMap
	}
	
	/**
	 * Get an optionally filtered map of roles with the specified key.
	 * @param ucdRoles The list of roles.
	 * @param match The match regular expression.
	 * @return The roles map.
	 */
	@JsonIgnore
	public static UcdRolesMap getRolesIdMap(
		final List<UcdRole> ucdRoles,
		final String match = null) {
		
		return getRolesMap(
			ucdRoles, 
			KEY_ID, 
			match
		)
	}
	
	/**
	 * Get an optionally filtered map of roles with the specified key.
	 * @param ucdRoles The list of roles.
	 * @param match The match regular expression.
	 * @return The roles map.
	 */
	@JsonIgnore
	public static UcdRolesMap getRolesNameMap(
		final List<UcdRole> ucdRoles,
		final String match = null) {
		
		return getRolesMap(
			ucdRoles, 
			KEY_NAME, 
			match
		)
	}
}
