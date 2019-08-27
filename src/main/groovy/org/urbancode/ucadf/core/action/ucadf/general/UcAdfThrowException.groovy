/**
 * This action is used to conditionally throw an exception.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcAdfThrowException extends UcAdfAction {
	// Action properties.
	/** The exception message. */
	String message
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		// Throw an exception message.
		throw new UcdInvalidValueException(message)
	}
}
