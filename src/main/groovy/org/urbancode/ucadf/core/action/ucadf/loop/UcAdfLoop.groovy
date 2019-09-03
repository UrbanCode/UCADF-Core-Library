/**
 * This abstract class is the superclass for the UCADF loop actions.
 */
package org.urbancode.ucadf.core.action.ucadf.loop

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionPropertyEnum

abstract class UcAdfLoop extends UcAdfAction {
	/** The actions to perform for each iteration. */
	List<HashMap> actions
	
	/** The number of seconds to wait before processing the next item. */	
	Integer waitIntervalSecs = 0
	
	// Protected properties.	
	protected Boolean loopBreak = false
	
	/**
	 * Process the actions and return true if a break was encountered.
	 */
	protected void processActions() {
		// Run the child actions.
		for (action in actions) {
			if (UcAdfLoopContinue.getSimpleName().equals(action.get(UcAdfActionPropertyEnum.ACTION.getPropertyName()))) {
				if (actionsRunner.runAction(action)) {
					break
				}
			} else if (UcAdfLoopBreak.getSimpleName().equals(action.get(UcAdfActionPropertyEnum.ACTION.getPropertyName()))) {
				if (actionsRunner.runAction(action)) {
					loopBreak = true
					break
				}
			} else {
				actionsRunner.runAction(action)
			}
		}
	}

	/**
	 * Process the optional wait interval.	
	 */
	protected void processWaitInterval() {
		if (waitIntervalSecs) {
			Thread.sleep(waitIntervalSecs * 1000)
		}
	}
}
