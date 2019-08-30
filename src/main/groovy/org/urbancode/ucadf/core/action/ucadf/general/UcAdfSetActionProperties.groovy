/**
 * This action is used to set the action properties to a given set of values.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfSetActionProperties extends UcAdfAction {
	// Action properties.
	/** If true then show debug information. */
	Boolean debug = false
	
	/** If true then set the properties so they will be evaluated once and made available to all actions running under the current actions runner. */
	Boolean setStatic = false
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// If setting static then replace the actions runner property values with the evaluated values.
		if (setStatic) {
			propertyValues.each { k, v ->
				actionsRunner.setPropertyValue(k, v)
			}
		}
	}
}
