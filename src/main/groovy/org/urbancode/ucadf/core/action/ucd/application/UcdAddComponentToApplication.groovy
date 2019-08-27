/**
 * This action adds a component to an application.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdAddComponentToApplication extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The component name or ID. */
	String component
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Adding component [$component] to application [$application].")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/addComponentToApp")
			.queryParam("application", application)
			.queryParam("component", component)
			
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		if (response.getStatus() == 200) {
			logInfo("Component [$component] added to application [$application].")
		} else {
            throw new UcdInvalidValueException(response)
		}
	}	
}
