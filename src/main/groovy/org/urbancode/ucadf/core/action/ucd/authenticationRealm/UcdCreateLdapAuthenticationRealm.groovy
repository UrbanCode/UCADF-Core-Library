/**
 * This action creates an LDAP authentication realm.
 */
package org.urbancode.ucadf.core.action.ucd.authenticationRealm

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.authorizationRealm.UcdGetAuthorizationRealm
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authenticationRealm.UcdAuthenticationRealm
import org.urbancode.ucadf.core.model.ucd.authenticationRealm.UcdAuthenticationRealmProperties
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealm
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import groovy.json.JsonBuilder

class UcdCreateLdapAuthenticationRealm extends UcAdfAction {
	// Action properties.
	/** The authentication realm name. */
    String name
	
	/** The description. */
    String description = ""
	
	/** The LDAP URL. */
    String ldapUrl
	
	/** The connection name. */
	String connectionName = ""
	
	/** The connection password. */
	UcAdfSecureString connectionPassword = new UcAdfSecureString()

	/** The user pattern. */
	String userPattern = ""
		
	/** The user base. */
    String userBase
	
	/** The user search. Default is uid={0}. */
    String userSearch = "uid={0}"
	
	/** The user name attribute. Deafult is cn. */
    String userNameAttribute = "cn"
	
	/** The user email attribute. Default is mail. */
    String userEmailAttribute = "mail"
	
	/** If true then the subtree is searched. Default is true. */
    Boolean searchSubtree = true
	
	/** The names or IDs of the authorization realms. */
	List<String> authorizationRealms

	/** The number of allowed login attempts. Default is 0. */
	Integer allowedAttempts = 0
	
	/** The flag that indicates fail if the tag already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the authentication realm was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		        
        logVerbose("Create an authentication realm.")
        WebTarget target = ucdSession.getUcdWebTarget().path("/security/authenticationRealm")
        logDebug("target=$target")

        // Build a custom post body that includes only the required fields
        Map<String, String> requestMap = [
            "name" : name,
            "description" : description,
            "allowedAttempts" : allowedAttempts,
            "loginClassName" : UcdAuthenticationRealm.MODULECLASSNAME_LDAP,
            "anonymousConnection" : connectionPassword.toString() ? false : true,
            "userSearchType" : "searchBase",
            "properties": [
                "context-factory" : UcdAuthenticationRealmProperties.CONTEXTFACTORY_LDAP,
                "url" : ldapUrl,
                "connection-password" : connectionPassword.toString(),
                "connection-name" : connectionName,
                "user-pattern" : userPattern,
                "user-base" : userBase,
                "user-search" : userSearch,
                "user-search-subtree" : searchSubtree,
                "name-attribute" : userNameAttribute,
                "email-attribute" : userEmailAttribute
            ]
        ]

		// Construct the list of authorization realm IDs.
		List<String> authorizationRealmIds = []
		for (authorizationRealm in authorizationRealms) {
			UcdAuthorizationRealm ucdAuthorizationRealm = actionsRunner.runAction([
				action: UcdGetAuthorizationRealm.getSimpleName(),
				realm: authorizationRealm,
				failIfNotFound: true
			])
			
			authorizationRealmIds.add(ucdAuthorizationRealm.getId())
		}

        // Handle difference between UCD 6.1 and UCD 6.2 API.
        if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_61)) {
            requestMap.put("authorizationRealm", authorizationRealmIds[0])
        } else {
            requestMap.put("authorizationRealms", authorizationRealmIds)
        }
        
        JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
        logVerbose(jsonBuilder.toString())
        
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
            logVerbose("Created authentication realm [$name].")
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			
			Boolean alreadyExists = false
			if ((response.getStatus() == 400 || response.getStatus() == 403) && errMsg ==~ /.*already exists.*/) {
				alreadyExists = true
			} else if (response.getStatus() == 500 && errMsg ==~ /.*after response has been committed.*/) {
				// UCD 7.0.4 is returning 500 Cannot forward after response has been committed if it already exists.
				alreadyExists = true
			}
			
			if (!alreadyExists || (alreadyExists && failIfExists)) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
    }
}
