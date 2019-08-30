/**
 * This action gets a component property.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetComponentProperty extends UcAdfAction {
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

	/** The property name or ID. */	
	String property
	
	/** The type to return. Default is OBJECT. */
	ReturnAsEnum returnAs = ReturnAsEnum.OBJECT
	
	/** The flag that indicates fail if the component property is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The property as either a string or a property object.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting component [$component] property [$property].")

		Object returnProperty
		
		if (returnAs == ReturnAsEnum.VALUE) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/getProperty")
				.queryParam("component", component)
				.queryParam("name", property)
			logDebug("target=$target")
	
			returnProperty = ""
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				returnProperty = response.readEntity(String.class)
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logInfo(errMsg)
				if (response.getStatus() != 404 || failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		} else {
			// Only way found to get a single property is to get all the properties then find the matching one.
			List<UcdProperty> ucdProperties = actionsRunner.runAction([
				action: UcdGetComponentProperties.getSimpleName(),
				component: component
			])

			returnProperty = ucdProperties.find {
				it.getName() == property
			}
		}

		return returnProperty
	}
}
