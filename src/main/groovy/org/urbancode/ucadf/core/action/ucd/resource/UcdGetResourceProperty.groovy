/**
 * This action gets a resource property.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.application.UcdGetApplicationProperty.ReturnAsEnum
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetResourceProperty extends UcAdfAction {
	/** The type of return. */
	enum ReturnAsEnum {
		/** Return as a string value. */
		VALUE,
		
		/** Return as a {@link UcdObject}. */
		OBJECT
	}
	
	/** The resource path or ID. */
	String resource

	/** The property name or ID. */	
	String property
	
	/** The type to return. Default is OBJECT. */
	ReturnAsEnum returnAs = ReturnAsEnum.OBJECT
	
	/** The flag that indicates fail if the resource property is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The property as either a string or a property object.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting resource [$resource] property [$property].")

		Object returnProperty
		
		if (returnAs == ReturnAsEnum.VALUE) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/getProperty")
				.queryParam("resource", resource)
				.queryParam("name", property)
			logDebug("target=$target")
	
			returnProperty = ""
			
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
				action: UcdGetResourceProperties.getSimpleName(),
				actionInfo: false,
				resource: resource,
				failIfNotFound: failIfNotFound
			])

			returnProperty = ucdProperties.find {
				it.getName() == property
			}
		}

		return returnProperty
	}
}
