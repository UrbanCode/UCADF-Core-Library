/**
 * This action creates an group in an authorization realm.
 */
package org.urbancode.ucadf.core.action.ucd.group

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.authorizationRealm.UcdGetAuthorizationRealm
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealm
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import groovy.json.JsonBuilder

class UcdCreateGroup extends UcAdfAction {
	// Action properties.
	/** The group name. */
    String name
	
	/** The authorization realm name or ID. */
	String authorizationRealm
	
	/** The flag that indicates fail if already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the group was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
        
		Boolean created = false
		
        logVerbose("Create group [$name] in authorization realm [$authorizationRealm].")
		
        WebTarget target = ucdSession.getUcdWebTarget().path("/security/group")
        logDebug("target=$target")

		// If an authorization realm ID was provided then use it. Otherwise get the authorization realm information to get the ID.
		String authorizationRealmId = authorizationRealm
		if (!UcdObject.isUUID(authorizationRealm)) {
			UcdAuthorizationRealm ucdAuthorizationRealm = actionsRunner.runAction([
				action: UcdGetAuthorizationRealm.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				realm: authorizationRealm
			])
			
			authorizationRealmId = ucdAuthorizationRealm.getId()
		}

        // Build a custom post body that includes only the required fields
		Map requestMap = [
			name : name,
            authorizationRealm: authorizationRealmId
		]
		
        JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
        logVerbose(jsonBuilder.toString())
            
        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
            logVerbose("Created group [$name] in authorization realm [$authorizationRealm].")
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
