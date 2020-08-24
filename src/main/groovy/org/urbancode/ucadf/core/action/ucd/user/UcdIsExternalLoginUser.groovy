/**
 * This action determines if the account belongs to a real user.
 * Example usage: This can be helpful when looking for human user accounts allowed to do approvals.
 */
package org.urbancode.ucadf.core.action.ucd.user

import org.urbancode.ucadf.core.action.ucd.authenticationRealm.UcdGetAuthenticationRealm
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authenticationRealm.UcdAuthenticationRealm
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdIsExternalLoginUser extends UcAdfAction {
	// Action properties.
	/** The user name or ID. */
	String user

	/** This flag indicates to determine if the user has an email address value also. */
	Boolean requireEmail = false
		
	/**
	 * Runs the action.	
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean isExternalLoginUser = false

		logVerbose("Determining if user [$user] is an external login user.")

		// Get the user information.				
		UcdUser ucdUser = actionsRunner.runAction([
			action: UcdGetUser.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			user: user,
			failIfNotFound: true
		])

		// Get the user's authentication realm information.
		UcdAuthenticationRealm authenticationRealm = actionsRunner.runAction([
			action: UcdGetAuthenticationRealm.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			realm: ucdUser.getAuthenticationRealm(),
			failIfNotFound: true
		])

		// Determine if the user belongs to an internal login module.
		if (!(authenticationRealm.getLoginModuleClassName() ==~ /.*InternalLoginModule.*/)) {
			if (!requireEmail || (requireEmail && ucdUser.getEmail())) {
				isExternalLoginUser = true
			}
		}
		
		logVerbose("Is external login user [$isExternalLoginUser].")
		
		return isExternalLoginUser
	}
}
