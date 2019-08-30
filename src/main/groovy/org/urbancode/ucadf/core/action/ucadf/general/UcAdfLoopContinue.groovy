/**
 * This action is used to skip the remainder of a loop and go to the next iteration.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfLoopContinue extends UcAdfAction {
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// If this is run then that means the when evaluated to true.
		logInfo("Loop continue is true.")
		
		return true
	}
}
