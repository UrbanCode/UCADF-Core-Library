/**
 * This action deletes all of a user's authentication tokens.
 */
package org.urbancode.ucadf.core.action.ucd.authToken

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authToken.UcdAuthToken

class UcdDeleteAuthTokens extends UcAdfAction {
	// Action properties.
	/** The user name or ID. */
	String user
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the authentication token is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Find the list of user authentication tokens.
		List<UcdAuthToken> ucdAuthTokens = actionsRunner.runAction([
			action: UcdGetAuthTokens.getSimpleName(),
			actionInfo: false,
			user: user
		])
		
		for (ucdAuthToken in ucdAuthTokens) {
			actionsRunner.runAction([
				action: UcdDeleteAuthToken.getSimpleName(),
				authToken: ucdAuthToken.getId(),
				commit: commit
			])
		}
	}	
}
