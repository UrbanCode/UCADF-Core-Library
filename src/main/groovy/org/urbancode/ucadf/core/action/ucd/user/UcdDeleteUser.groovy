/**
 * This action deletes a user.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.authToken.UcdDeleteAuthTokens
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdDeleteUser extends UcAdfAction {
	// Action properties.
	/** The user name or ID. */
	String user
	
	/** The flag that indicates fail if the user is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/**
	 * Runs the action.	
	 * @return True if the user was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		if (!commit) {
			logVerbose("Would delete user [$user]")
		} else {
			// Don't leave any abandoned auth tokens.
			actionsRunner.runAction([
				action: UcdDeleteAuthTokens.getSimpleName(),
				user: user
			])

	        // UCD 6.1 returns 500 if not found, UCD 6.2 returns 404.		
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/user/{userName}")
				.resolveTemplate("userName", user)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.WILDCARD).delete()
			if (response.getStatus() == 200) {
				logVerbose("User [$user] deleted.")
				deleted = true
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				// Some older versions return 500.
				if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}
		
		return deleted
	}	
}
