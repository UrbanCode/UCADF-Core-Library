/**
 * This action adds a component to an application.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

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

		logVerbose("Adding component [$component] to application [$application].")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/addComponentToApp")
			.queryParam("application", application)
			.queryParam("component", component)
			
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		if (response.getStatus() == 200) {
			logVerbose("Component [$component] added to application [$application].")
		} else {
            throw new UcAdfInvalidValueException(response)
		}
	}	
}
