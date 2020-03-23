/**
 * This action gets the application properties.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetApplicationProperties extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/**
	 * Runs the action.
	 * @return The list of property objects.
	 */
	public List<UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting application [$application] properties.")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/getProperties")
			.queryParam("application", application)
		logDebug("target=$target")

		List<UcdProperty> ucdProperties
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdProperties = response.readEntity(new GenericType<List<UcdProperty>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		return ucdProperties
	}
}
