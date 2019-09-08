/**
 * This action is used to add property values to the runner outProps property that may subsequently be returned to plugin step.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfAddOutProps extends UcAdfAction {
	// Action properties.
	/** If true then show debug information. */
	Boolean debug = false
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		propertyValues.each { k, v ->
			outProps.put(k, v)
		}
	}
}
