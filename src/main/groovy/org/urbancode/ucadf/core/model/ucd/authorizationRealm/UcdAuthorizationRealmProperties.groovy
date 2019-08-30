/**
 * This class instantiates authorization realm property objects.
 */
package org.urbancode.ucadf.core.model.ucd.authorizationRealm

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

// TODO: Does this need to become a specialized class for LDAP or just more properties added?
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAuthorizationRealmProperties extends UcdObject {
	public final static String GROUPMAPPER = "00000000000000000000000000000000"
	
	String url
	
	@JsonProperty("connection-name")
	String connectionName
	@JsonProperty("connection-password")
	String connectionPassword
	
	@JsonProperty("context-factory")
	String contextFactory
	
	@JsonProperty("group-name")
	String groupName
	@JsonProperty("group-base")
	String groupBase
	@JsonProperty("group-attribute")
	String groupAttribute
	@JsonProperty("group-mapper")
	String groupMapper
	@JsonProperty("group-search")
	String groupSearch
	@JsonProperty("group-search-subtree")
	Boolean groupSearchSubtree
	
	@JsonProperty("user-base")
	String userBase
	@JsonProperty("user-pattern")
	String userPattern
	@JsonProperty("user-search-subtree")
	Boolean userSearchSubtree
	@JsonProperty("user-search")
	Boolean userSearch

	// Constructors.	
	UcdAuthorizationRealmProperties() {
	}
}
