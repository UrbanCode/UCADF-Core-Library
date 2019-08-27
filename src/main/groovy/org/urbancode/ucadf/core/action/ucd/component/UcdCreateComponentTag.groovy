/**
 * This action creates a component tag.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdColorEnum
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import groovy.json.JsonBuilder

class UcdCreateComponentTag extends UcAdfAction {
	// Action properties.
	/** The tag name. */
	String name
	
	/** The description. */
	String description = ""
	
	/** The color. */
	UcdColorEnum color
	
	/** The component name or ID to use temporarily to create the tag since this is the only way the API can do it. The tag will be removed from the component after it is created. */
	String component
	
	/** The flag that indicates fail if the component tag already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the component tag was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
	
		Boolean created = false
			
		// Get the tag if it exists.
		UcdTag ucdTag = actionsRunner.runAction([
			action: UcdGetComponentTag.getSimpleName(),
			tag: name,
			failIfNotFound: false
		])

		if (ucdTag) {
			if (failIfExists) {
				throw new UcdInvalidValueException("Component tag [$name] already exists.")
			} else {
				logInfo("Component tag [$name] already exists.")
			}
		} else {
			logInfo("Create component tag [$name].")
		
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				component: component,
				failIfNotFound: true
			])
			
			// Used the undocumented API because the cli REST interface wasn't dealing with the color parameter.
			// Build a custom post body that includes only the required fields.
			Map requestMap = [
				name: name, 
				description: description, 
				color: color.getValue(), 
				url: "/rest/tag/Component", 
				ids: [ ucdComponent.getId() ]
			]

			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/tag/Component")
			logDebug("target=$target")
	
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 204) {
	            logInfo("Component tag [$name] created.")
				created = true
			} else {
				throw new UcdInvalidValueException(response)
			}
			
			// Remove the tag from where it was temporarily created.
			actionsRunner.runAction([
				action: UcdRemoveTagsFromComponent.getSimpleName(),
				component: ucdComponent.getName(),
				tags: [ name ]
			])
		}
		
		return created
	}
}
