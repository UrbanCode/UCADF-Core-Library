/**
 * This action gets a user.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdGetUser extends UcAdfAction {
	// Action properties.
	/** The user name or ID. */
	String user
	
	/** The flag that indicates fail if the user is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The user object.
	 */
	@Override
	public UcdUser run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting user [$user].")
		
		UcdUser ucdUser

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/user/info")
			.queryParam("user", user)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdUser = response.readEntity(UcdUser.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		return ucdUser
	}
}
