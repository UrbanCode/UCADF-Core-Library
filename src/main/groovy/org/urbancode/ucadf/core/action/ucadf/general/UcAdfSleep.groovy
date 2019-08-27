/**
 * This action is used to sleep for the specified number of seconds.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfSleep extends UcAdfAction {
	// Action properties.
	/** The number of seconds to sleep. */
	Long seconds
	
	/** (Optional) The comment to display before sleeping. */
	String comment = ""
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		if (comment) {
			logInfo(comment)
		} else {
			logInfo("Sleeping for [$seconds] seconds.")
		}
		
		Thread.sleep(seconds * 1000)
	}
}
