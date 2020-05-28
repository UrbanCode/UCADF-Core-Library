/**
 * This action creates a resource tag.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdColorEnum
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import groovy.json.JsonBuilder

class UcdCreateResourceTag extends UcAdfAction {
	// Action properties.
	/** The resource tag name. */
	String name
	
	/** (Optional) The description. */
	String description = ""
	
	/** The color. */
	UcdColorEnum color
	
	/** The resource name or ID to use temporarily to create the tag since this is the only way the API can do it. The tag will be removed from the resource after it is created. */
	String resource
	
	/** The flag that indicates fail if the resource tag already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Get the tag if it exists.
		UcdTag ucdTag = actionsRunner.runAction([
			action: UcdGetResourceTag.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			actionVerbose: actionVerbose,
			tag: name,
			failIfNotFound: false
		])

		if (ucdTag) {
			if (failIfExists) {
				throw new UcAdfInvalidValueException("Resource tag [$name] already exists.")
			} else {
				logVerbose("Resource tag [$name] already exists.")
			}
		} else {
			logVerbose("Create resource tag [$name].")
		
			UcdResource ucdResource = actionsRunner.runAction([
				action: UcdGetResource.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				resource: resource,
				failIfNotFound: true
			])
			
			// Used the undocumented API because the cli REST interface wasn't dealing with the color parameter.
			// Build a custom post body that includes only the required fields.
			Map requestMap = [
				name: name, 
				description: description, 
				color: color.getValue(), 
				url: "/rest/tag/Resource", 
				ids: [ ucdResource.getId() ]
			]

			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/tag/Resource")
			logDebug("target=$target")
	
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 204) {
	            logVerbose("Resource tag [$name] created.")
			} else {
				throw new UcAdfInvalidValueException(response)
			}
			
			// Remove the tag from where it was temporarily created.
			actionsRunner.runAction([
				action: UcdRemoveTagsFromResource.getSimpleName(),
				actionInfo: false,
				resource: ucdResource.getId(),
				tags: [ name ]
			])
		}
	}
}
