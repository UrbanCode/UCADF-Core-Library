/**
 * This action creates an LDAP authorization realm.
 */
package org.urbancode.ucadf.core.action.ucd.authorizationRealm

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealm
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealmProperties

import groovy.json.JsonBuilder

class UcdCreateLdapAuthorizationRealm extends UcAdfAction {
	// Action properties.
	/** The authorization realm name. */
    String name
	
	/** The description. */
    String description = ""

	/** LDAP configuration properties. If a value is provided it takes precedence over the individually specified properties above. */
	Map<String, String> configProperties = [:]

	/** The group attribute. Deprecated. Use the group-attribute configProperties value. */
	@Deprecated
	String groupAttribute = ""
		
	/** The group base. Deprecated. Use the group-base configProperties value. */
	@Deprecated
    String groupBase = ""
	
	/** The group search. Default is uniquemember={0}. Deprecated. Use the group-search configProperties value. */
	@Deprecated
    String groupSearch = "uniquemember={0}"
	
	/** The group name attributes. Default is cn. Deprecated. Use the group-name configProperties value. */
	@Deprecated
    String groupName = "cn"
	
	/** If true then the subtree is searched. Default is true. Deprecated. Use the group-search-subtree configProperties value. */
	@Deprecated
    Boolean searchSubtree = true
	
	/** The flag that indicates fail if the tag already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the authorization realm was created.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
        
		Boolean created = false
		
        logVerbose("Create authorization realm [$name].")
		
        WebTarget target = ucdSession.getUcdWebTarget().path("/security/authorizationRealm")
        logDebug("target=$target")

		// Construct the request LDAP configuration properties.
		Map<String, String> requestConfigProperties = [
	        "group-attribute" : groupAttribute,
	        "group-base" : groupBase,
	        "group-search" : groupSearch,
	        "group-name" : groupName,
	        "group-search-subtree" : searchSubtree,
	        "group-mapper" : UcdAuthorizationRealmProperties.GROUPMAPPER
		]

		// Override the default map with any additionally provided configuration properties.
		configProperties.each { k, v ->
			requestConfigProperties.put(k, v)
		}

        // Build a custom post body that includes only the required fields
		Map requestMap = [
			name : name,
            description : description,
            authorizationModuleClassName : UcdAuthorizationRealm.AUTHORIZATIONMODULE_LDAP,
            groupSearchType : "roleSearch",
            properties: requestConfigProperties
		]
		
        JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
        logVerbose(jsonBuilder.toString())
            
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
            logVerbose("Created authorization realm [$name].")
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			
			Boolean alreadyExists = false
			if ((response.getStatus() == 400 || response.getStatus() == 403) && errMsg ==~ /.*already exists.*/) {
				alreadyExists = true
			} else if (response.getStatus() == 500) {
				// UCD 7.0.4 and/or 7.0.5 are returning 500 if it already exists.
				alreadyExists = true
			}
			
			if (!alreadyExists || (alreadyExists && failIfExists)) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
    }
}
