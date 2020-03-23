/**
 * This action creates an application.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

import groovy.json.JsonBuilder

class UcdCreateApplication extends UcAdfAction {
	// Action properties.
	/** The application name. */
	String name

	/** (Optional) The application description. */	
	String description = ""
	
	/** The flag that indicates fail if the application already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.
	 * @return True if the application was created.
	 */
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		logVerbose("Creating application [$name].")

		// Initialize the request.
		Map requestMap = [
			name: name,
			description: description
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/create")
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Application [$name] created.")
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
	}	
}
