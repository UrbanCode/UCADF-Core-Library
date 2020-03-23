/**
 * This action creates an agent tag.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdColorEnum
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import groovy.json.JsonBuilder

class UcdCreateAgentTag extends UcAdfAction {
	// Action properties.
	/** The tag name. */
	String name
	
	/** The tag color. */
	UcdColorEnum color
	
	/** (Optional) The tag description. */
	String description = ""
	
	/** The flag that indicates fail if the tag already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.
	 * @return True if the agent tag was created.
	 */
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
				
		// Get the tag if it exists.
		UcdTag ucdTag = actionsRunner.runAction([
			action: UcdGetAgentTag.getSimpleName(),
			tag: name,
			failIfNotFound: false
		])

		if (ucdTag) {
			if (failIfExists) {
				throw new UcAdfInvalidValueException("Agent tag [$name] already exists.")
			} else {
				logVerbose("Agent tag [$name] already exists.")
			}
		} else {
			logVerbose("Create agent tag [$name].")
		
			// The only way to create a new tag is by adding it to an agent temporarily.
			List<UcdAgent> ucdAgents = actionsRunner.runAction([
				action: UcdGetAgents.getSimpleName()
			])
	
			if (ucdAgents.size() < 1) {
				throw new UcAdfInvalidValueException("No agents found to add tag.")
			}
			
			UcdAgent ucdAgent = ucdAgents[0]
	
			// Used the undocumented API because the cli REST interface wasn't dealing with the color parameter.
			// Build a custom post body that includes only the required fields.
			Map requestMap = [
				name: name, 
				description: description, 
				color: color.getValue(), 
				url: "/rest/tag/Agent", 
				ids: [ ucdAgent.getId() ]
			]

			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/tag/Agent")
			logDebug("target=$target")
	
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 204) {
	            logVerbose("Agent tag [$name] created.")
				created = true
			} else {
				throw new UcAdfInvalidValueException(response)
			}
			
			// Remove the tag from where it was temporarily created.
			actionsRunner.runAction([
				action: UcdRemoveTagsFromAgent.getSimpleName(),
				agent: ucdAgent.getName(),
				tags: [ name ]
			])
		}
		
		return created
	}
}
