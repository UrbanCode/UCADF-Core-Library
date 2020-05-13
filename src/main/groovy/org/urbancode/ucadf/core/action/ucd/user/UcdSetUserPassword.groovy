/**
 * This action sets a user password.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

import groovy.json.JsonBuilder

// The session that is running this must have permissions to manage security in order to get the team ID.
class UcdSetUserPassword extends UcAdfAction {
	// Action properties.
	/** The user name or ID. */
	String user
	
	/** The password. */
	UcAdfSecureString password
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Setting user [$user] password.")

		// If an user ID was provided then use it. Otherwise get the user information to get the ID.
		String userId = user
		if (!UcdObject.isUUID(user)) {
			UcdUser ucdUser = actionsRunner.runAction([
				action: UcdGetUser.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				user: user,
				failIfNotFound: true
			])
			userId = ucdUser.getId()
		}

		WebTarget target = ucdSession.getUcdWebTarget().path("/security/user/{userId}/setPassword")
			.resolveTemplate("userId", userId)
		logDebug("target=$target")
		
		// Build a custom post body that includes only the required fields
		Map requestMap = [
			password: password.toString()
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		Response response = target.request(MediaType.WILDCARD).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("User [$user} password set.")
		} else {
			throw new UcAdfInvalidValueException(response)
		}
	}
}
