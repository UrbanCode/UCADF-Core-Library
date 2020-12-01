/**
 * This action gets a list of a component template's properties.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetComponentTemplateProperties extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as List<UcdProperty>. */
		LIST,
		
		/** Return as Map<String, UcdProperty> having the property name as the key. */
		MAPBYNAME
	}

	// Action properties.
	/** The component template name or ID. */
	String componentTemplate

	/** The type of collection to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.LIST
	
	/**
	 * Runs the action.	
	 * @return The specified type of collection.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting component template [$componentTemplate] properties.")

		List<UcdProperty> ucdProperties = []
		
		UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
			action: UcdGetComponentTemplate.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			componentTemplate: componentTemplate,
			failIfNotFound: true
		])

		// Return as requested.
		Object componentTemplateProperties
		if (ReturnAsEnum.LIST.equals(returnAs)) {
			componentTemplateProperties = ucdComponentTemplate.getPropValues()
		} else {
			componentTemplateProperties = [:]
			for (ucdProperty in ucdComponentTemplate.getPropValues()) {
				componentTemplateProperties.put(ucdProperty.getName(), ucdProperty)
			}
		}

		return componentTemplateProperties
	}
}
