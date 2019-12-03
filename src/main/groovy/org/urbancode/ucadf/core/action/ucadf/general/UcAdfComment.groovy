/**
 * This action is used to show a comment during action processing.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfComment extends UcAdfAction {
	// Action properties.
	/** The comment string. This string may have printf-style placeholders, e.g. %d, to be replaced by the values. */
	String comment
	
	/** The list of values to inject into the comment string. */
	List values = []

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'ucdSession' ])
		
		printf("$comment\n", values)
	}
}
