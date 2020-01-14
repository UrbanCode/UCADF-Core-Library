/**
 * This action creates a user.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authenticationRealm.UcdAuthenticationRealm
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdSecureString

import groovy.json.JsonBuilder

class UcdCreateUser extends UcAdfAction {
	// Action properties.
	/** The user name. */
	String name
	
	/** The password. */
	UcdSecureString password
	
	/** (Optional) The actual name. */
	String actualName = ""
	
	/** (Optional) The email. */
	String email = ""
	
	/** The authentication realm name or ID. Default is internal. */
	String authenticationRealm = UcdAuthenticationRealm.AUTHENTICATIONREALM_INTERNAL_SECURITY
	
	/** The flag that indicates fail if the user already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the user was created.
	 */
	@Override
	public Boolean run() {
		// Validate the actions properties.
		validatePropsExist()

		Boolean created = false
		
		logInfo("Creating user [$name].")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/user")
		logDebug("target=$target")
		
		// Build a custom post body that includes only the required fields.
		Map requestMap = [
			name: name,
			password: password.toString(),
			actualName: actualName,
			email: email,
			authenticationRealm: authenticationRealm
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		
		Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logInfo("User [$name] created.")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			
			Boolean alreadyExists = false
			if (response.getStatus() == 403 && errMsg ==~ /.*already exists.*/) {
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
