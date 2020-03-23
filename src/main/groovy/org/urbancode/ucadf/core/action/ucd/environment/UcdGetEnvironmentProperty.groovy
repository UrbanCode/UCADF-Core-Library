/**
 * This action gets an environment property.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetEnvironmentProperty extends UcAdfAction {
	/** The type of return. */
	enum ReturnAsEnum {
		/** Return as a string value. */
		VALUE,
		
		/** Return as a {@link UcdObject}. */
		OBJECT
	}
	
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

	/** The property name. */	
	String property
	
	/** The type to return. Default is OBJECT. */
	ReturnAsEnum returnAs = ReturnAsEnum.OBJECT

	/** The flag that indicates fail if the environment property is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The property as either a string or a property object.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		Object returnProperty

		if (returnAs == ReturnAsEnum.VALUE) {
			returnProperty = ""
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/getProperty")
				.queryParam("application", application)
				.queryParam("environment", environment)
				.queryParam("name", property)
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				returnProperty = response.readEntity(String.class)
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() != 404 || failIfNotFound) {
		            throw new UcAdfInvalidValueException(errMsg)
				}
			}
		} else {
			// Only way found to get a single property is to get all the properties then find the matching one.
			List<UcdProperty> ucdProperties = actionsRunner.runAction([
				action: UcdGetEnvironmentProperties.getSimpleName(),
				application: application,
				environment: environment
			])

			returnProperty = ucdProperties.find {
				it.getName() == property
			}
		}
		
		// Return either the property value or the property.
		return returnProperty
	}
}
