/**
 * This action deletes an authentication token.
 */
package org.urbancode.ucadf.core.action.ucd.authToken

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdDeleteAuthToken extends UcAdfAction {
	// Action properties.
	/** The authentication token ID. */
	String authToken
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the authentication token is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 * @Return True if the authentication token was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		if (commit) {
			logVerbose("Deleting authentication token [$authToken].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/authtoken/{tokenId}")
				.resolveTemplate("tokenId", authToken)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 200) {
				logVerbose("Auth token [$authToken] deleted.")
				deleted = true
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				// Some versions return 500.
				if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		} else {
			logVerbose("Would delete authentication token [$authToken] token.")
		}
		
		return deleted
	}	
}
