/**
 * This action creates a component template.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentTypeEnum
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import groovy.json.JsonBuilder

class UcdCreateComponentTemplate extends UcAdfAction {
	// Action properties.
	/** The name. */
	String name

	/** The component type. */
	UcdComponentTypeEnum componentType
	
	/** (Optional) The description. */
	String description = ""
	
	/** The flag that indicates fail if the component template exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the component template was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		logInfo("Creating component template [$name].")
		
		Map requestMap = [
			name: name,
			description: description,
			componentType: componentType
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=$jsonBuilder")
			
		// Create the component template.
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentTemplate")
        logDebug("target=$target")

        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
			logInfo("Component template [$name] created.")
			created = true
        } else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
        }
		
		return created
	}
}
