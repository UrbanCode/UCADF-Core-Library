/**
 * This action gets a list of a component template's properties.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetComponentTemplateProperties extends UcAdfAction {
	// Action properties.
	/** The component template name or ID. */
	String componentTemplate
	
	/**
	 * Runs the action.	
	 * @return The list of property objects.
	 */
	@Override
	public List<UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting component template [$componentTemplate] properties.")

		List<UcdProperty> ucdProperties = []
		
		UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
			action: UcdGetComponentTemplate.getSimpleName(),
			componentTemplate: componentTemplate,
			failIfNotFound: true
		])

		return ucdComponentTemplate.getPropValues()
	}
}
