/**
 * This action is used to break out of the current loop.
 */
package org.urbancode.ucadf.core.action.ucadf.loop

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfLoopBreak extends UcAdfAction {
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// If this is run then that means the when evaluated to true.
		logVerbose("Loop break is true.")
		return true
	}
}
