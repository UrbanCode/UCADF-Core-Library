/**
 * This action creates a component configuration template.
 */
package org.urbancode.ucadf.core.action.ucd.componentConfigTemplate

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import groovy.json.JsonBuilder

class UcdCreateComponentConfigTemplate extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The component template name. */
	String name
	
	/** The component template data. */
	String data
	
	/** The flag that indicates fail if the component configuration template already exists. Default is true. */
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
		
		logVerbose("Creating component [$component] configuration template [$name].")

		// If an component ID was provided then use it. Otherwise get the component information to get the ID.
		String componentId = component
		if (!UcdObject.isUUID(component)) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				component: component,
				failIfNotFound: true
			])
			
			componentId = ucdComponent.getId()
		}

		// Initialize the request.
		Map requestMap = [
			name: name,
			data: data,
			componentId: componentId
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap) 
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/configTemplate")
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Component [$component] configuration template [$name] created.")
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			
			Boolean alreadyExists = false
			if (response.getStatus() == 405) {
				alreadyExists = true
			} else if (response.getStatus() == 500) {
				// UCD 7.0.4 and/or 7.0.5 are returning 500 if it already exists.
				alreadyExists = true
			}
			
			if (!alreadyExists || (alreadyExists && failIfExists)) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
	}	
}
