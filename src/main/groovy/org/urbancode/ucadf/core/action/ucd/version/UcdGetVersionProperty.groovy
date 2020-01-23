/**
 * This action gets a version property.
 */
package org.urbancode.ucadf.core.action.ucd.version

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetVersionProperty extends UcAdfAction {
	/** The type of return. */
	enum ReturnAsEnum {
		/** Return as a string value. */
		VALUE,
		
		/** Return as a {@link UcdObject}. */
		OBJECT
	}
	
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The property name or ID. */
	String property
	
	/** The type to return. Default is OBJECT. */
	ReturnAsEnum returnAs = ReturnAsEnum.OBJECT
	
	/** The flag that indicates fail if the version property is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The property as either a string or a property object.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		Object returnProperty = ""

		logVerbose("Getting component [$component] version [$version] property [$property].")

		Map<String, UcdProperty> propertiesMap = actionsRunner.runAction([
			action: UcdGetVersionProperties.getSimpleName(),
			actionInfo: false,
			component: component,
			version: version,
			failIfNotFound: failIfNotFound
		])

		UcdProperty ucdProperty = propertiesMap.get(property)
		if (ucdProperty) {
			if (returnAs == ReturnAsEnum.VALUE) {
				returnProperty = ucdProperty.getValue()
			} else {
				returnProperty = ucdProperty
			}
		} else {
			if (failIfNotFound) {
				throw new UcdInvalidValueException("Component [$component] version [$version] property [$property] not found.")
			}
		}
		
		return returnProperty
	}
}
