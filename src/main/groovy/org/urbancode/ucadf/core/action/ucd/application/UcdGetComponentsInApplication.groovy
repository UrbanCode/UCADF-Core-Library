/**
 * This action gets a list of components in an application.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetComponentsInApplication extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/**
	 * Runs the action.
	 * @return The list of component objects.
	 */
	public List<UcdComponent> run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting application [$application] components.")

		List<UcdComponent> ucdComponents
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/componentsInApplication")
			.queryParam("application", application)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponents = response.readEntity(new GenericType<List<UcdComponent>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}

		return ucdComponents
	}
}
