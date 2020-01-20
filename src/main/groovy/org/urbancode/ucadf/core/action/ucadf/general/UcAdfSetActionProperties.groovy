/**
 * This action is used to set the action properties to a given set of values.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

class UcAdfSetActionProperties extends UcAdfAction {
	// Action properties.
	/** If true then show debug information. */
	Boolean debug = false

	Boolean setFinal = false
		
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Set the property values.		
		actionsRunner.setPropertyValues(propertyValues)

		if (setFinal) {
			propertyValues.keySet().each { propertyName ->
				if (!actionsRunner.finalPropertyNames.contains(propertyName)) {
					actionsRunner.finalPropertyNames.add(propertyName)
				}
			}		
		}
	}
}
