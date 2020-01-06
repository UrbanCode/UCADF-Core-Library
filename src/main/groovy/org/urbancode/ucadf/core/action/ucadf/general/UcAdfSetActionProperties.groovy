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
	
	/** The list of deferred property values. These will not be evaluated until the action is run. */
	Map<String, Object> deferredPropertyValues = new TreeMap<String, Object>()
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Set the deferred properties.		
		actionsRunner.setPropertyValues(deferredPropertyValues)
	}
}
