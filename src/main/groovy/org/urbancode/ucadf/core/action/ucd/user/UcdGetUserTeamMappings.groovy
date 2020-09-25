/**
 * This action gets a user.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.user.UcdUserTeamMapping

class UcdGetUserTeamMappings extends UcAdfAction {
	// Action properties.
	/** The user name or ID. */
	String user
	
	/** The flag that indicates fail if the user is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The list of user team mappings.
	 */
	@Override
	public List<UcdUserTeamMapping> run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting user [$user] role mappings.")
		
		List<UcdUserTeamMapping> userTeamMappings

		WebTarget target = ucdSession.getUcdWebTarget().path("/security/user/roleMappings/{user}")
			.resolveTemplate("user", user)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			userTeamMappings = response.readEntity(new GenericType<List<UcdUserTeamMapping>>(){})
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}

		return userTeamMappings
	}
}
