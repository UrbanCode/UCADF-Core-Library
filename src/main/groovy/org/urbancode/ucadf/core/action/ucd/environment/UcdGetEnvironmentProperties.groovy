/**
 * This action gets an environment's properties.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetEnvironmentProperties extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list. */
		LIST,
		
		/** Return as a map having the property name as the key. */
		MAPBYNAME
	}

	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
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

		Object environmentProperties
		
		logVerbose("Getting application [$application] environment [$environment] properties.")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/getProperties")
			.queryParam("application", application)
			.queryParam("environment", environment)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			List<UcdProperty> ucdProperties = response.readEntity(new GenericType<List<UcdProperty>>(){})
			if (ReturnAsEnum.LIST.equals(returnAs)) {
				environmentProperties = ucdProperties
			} else {
				environmentProperties = [:]
				for (ucdProperty in ucdProperties) {
					environmentProperties.put(ucdProperty.getName(), ucdProperty)
				}
			}
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		return environmentProperties
	}
}
