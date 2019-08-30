/**
 * This class instantiates authentication realm property objects.
 */
package org.urbancode.ucadf.core.model.ucd.authenticationRealm

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// TODO: Does this need to become a specialized class for LDAP or just more properties added?
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAuthenticationRealmProperties extends UcdObject {
	public final static String CONTEXTFACTORY_LDAP = "com.sun.jndi.ldap.LdapCtxFactory"

	// LDAP properties.	
	String url
	
	@JsonProperty("connection-name")
	String connectionName
	@JsonProperty("connection-password")
	String connectionPassword

	@JsonProperty("context-factory")
	String contextFactory
	
	@JsonProperty("user-search-subtree")
	Boolean userSearchSubtree
	@JsonProperty("user-base")
	String userBase
	@JsonProperty("user-pattern")
	String userPattern
	@JsonProperty("user-search")
	String userSearch
	@JsonProperty("name-attribute")
	String nameAttribute
	@JsonProperty("email-attribute")
	String emailAttribute
		
	// Constructors.	
	UcdAuthenticationRealmProperties() {
	}
}
