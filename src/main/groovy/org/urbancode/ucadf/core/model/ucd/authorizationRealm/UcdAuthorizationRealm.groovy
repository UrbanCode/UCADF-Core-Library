/**
 * This class instantiates authorization realm objects.
 */
package org.urbancode.ucadf.core.model.ucd.authorizationRealm

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAuthorizationRealm extends UcdObject {
	public final static String AUTHORIZATIONMODULE_LDAP = "com.urbancode.security.authorization.ldap.LdapAuthorizationModule"

	/** The authorization realm ID. */	
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The authorization module class name. */
	String authorizationModuleClassName
	
	/** Therealm properties. */
	UcdAuthorizationRealmProperties properties
	
	// Constructors.	
	UcdAuthorizationRealm() {
	}
}
