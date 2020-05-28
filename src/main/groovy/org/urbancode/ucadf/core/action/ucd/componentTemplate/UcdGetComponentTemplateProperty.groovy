/**
 * This action gets a component template property..
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetComponentTemplateProperty extends UcAdfAction {
	/** The type of return. */
	enum ReturnAsEnum {
		/** Return as a string value. */
		VALUE,
		
		/** Return as a {@link UcdObject}. */
		OBJECT
	}
	
	// Action properties.
	/** The component template name or ID. */
	String componentTemplate
	
	/** The property name. */
	String property
	
	/** The type to return. Default is OBJECT. */
	ReturnAsEnum returnAs = ReturnAsEnum.OBJECT
	
	/** The flag that indicates fail if the component template property is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The property as either a string or a property object.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting component template [$componentTemplate] property [$property].")

		// Get the component template information.		
		UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
			action: UcdGetComponentTemplate.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			componentTemplate: componentTemplate,
			failIfNotFound: true
		])

		UcdProperty ucdProperty = ucdComponentTemplate.getPropValues().find {
			(it.getName() == property)
		}

		if (!ucdProperty && failIfNotFound) {
			throw new UcAdfInvalidValueException("Component template [$componentTemplate] property [$property] not found.")
		}

		// Return either the property value or the property.
		return (returnAs == ReturnAsEnum.VALUE ? (ucdProperty ? ucdProperty.getValue() : "") : ucdProperty)
	}
}
