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
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
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
		
		logInfo("Creating component [$component] configuration template [$name].")

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
			logInfo("Component [$component] configuration template [$name] created.")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() != 405 || failIfExists) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return created
	}	
}
