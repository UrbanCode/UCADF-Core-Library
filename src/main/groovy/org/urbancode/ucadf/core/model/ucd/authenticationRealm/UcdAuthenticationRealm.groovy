/**
 * This class instantiates authentication realm objects.
 */
package org.urbancode.ucadf.core.model.ucd.authenticationRealm

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAuthenticationRealm extends UcdObject {
	public final static String AUTHENTICATIONREALM_INTERNAL_SECURITY = "Internal Security"

	public final static String MODULECLASSNAME_LDAP = "com.urbancode.security.authentication.ldap.LdapLoginModule"
	
	/** The ID. */	
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The flag that indicates read only. */
	Boolean readOnly
	
	/** The flag that indicates enabled. */
	Boolean enabled
	
	/** The flag that indicatesd ghosted. */
	Boolean ghosted
	
	/** The number of allowed login attempts. */
	Long allowedAttempts
	
	/** The sort order. */
	Long sortOrder
	
	/** The login module class name. */
	String loginModuleClassName
	
	/** The list of authorization realm IDs. */
	List<String> authorizationRealmIds

	/** The realm properties. */	
	UcdAuthenticationRealmProperties properties
	
	// Constructors.	
	UcdAuthenticationRealm() {
	}
}
