/**
 * This action is used to exit the actions runner processing.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfExit extends UcAdfAction {
	// Action properties.
	/** The exit code. Default is 0. */
	Integer exitCode = 0
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		System.exit(exitCode)
	}
}
