/**
 * This action gets an application's environments.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetEnvironments extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/**
	 * Runs the action.	
	 * @return The list of environment objects.
	 */
	@Override
	public List<UcdEnvironment> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdEnvironment> ucdEnvironments = []
		
		logInfo("Getting application [$application] environments.")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/environmentsInApplication")
			.queryParam("application", application)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdEnvironments = response.readEntity(new GenericType<List<UcdEnvironment>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdEnvironments
	}
}
