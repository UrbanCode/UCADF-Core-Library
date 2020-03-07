/**
 * This action creates an LDAP authorization realm.
 */
package org.urbancode.ucadf.core.action.ucd.authorizationRealm

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealm
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealmProperties
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import groovy.json.JsonBuilder

class UcdCreateLdapAuthorizationRealm extends UcAdfAction {
	// Action properties.
	/** The authorization realm name. */
    String name
	
	/** The description. */
    String description = ""

	/** The group attribute. */
	String groupAttribute = ""
		
	/** The group base. */
    String groupBase
	
	/** The group search. Default is uniquemember={0}. */
    String groupSearch = "uniquemember={0}"
	
	/** The group name attributes. Default is cn. */
    String groupName = "cn"
	
	/** If true then the subtree is searched. Default is true. */
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
        
        // Build a custom post body that includes only the required fields
		Map requestMap = [
			name : name,
            description : description,
            authorizationModuleClassName : UcdAuthorizationRealm.AUTHORIZATIONMODULE_LDAP,
            groupSearchType : "roleSearch",
            properties: [
                "group-attribute" : groupAttribute,
                "group-base" : groupBase,
                "group-search" : groupSearch,
                "group-name" : groupName,
                "group-search-subtree" : searchSubtree,
                "group-mapper" : UcdAuthorizationRealmProperties.GROUPMAPPER
            ]
		]
		
        JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
        logVerbose(jsonBuilder.toString())
            
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
            logVerbose("Created authorization realm [$name].")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			
			Boolean alreadyExists = false
			if ((response.getStatus() == 400 || response.getStatus() == 403) && errMsg ==~ /.*already exists.*/) {
				alreadyExists = true
			} else if (response.getStatus() == 500 && errMsg ==~ /.*after response has been committed.*/) {
				// UCD 7.0.4 is returning 500 Cannot forward after response has been committed if it already exists.
				alreadyExists = true
			}
			
			if (!alreadyExists || (alreadyExists && failIfExists)) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return created
    }
}
