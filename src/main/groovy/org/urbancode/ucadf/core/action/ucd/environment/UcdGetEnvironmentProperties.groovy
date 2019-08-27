/**
 * This action gets an environment's properties.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetEnvironmentProperties extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/**
	 * Runs the action.	
	 * @return The list of property objects.
	 */
	@Override
	public List<UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdProperty> ucdProperties
		
		logInfo("Getting application [$application] environment [$environment] properties.")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/getProperties")
			.queryParam("application", application)
			.queryParam("environment", environment)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdProperties = response.readEntity(new GenericType<List<UcdProperty>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdProperties
	}
}
