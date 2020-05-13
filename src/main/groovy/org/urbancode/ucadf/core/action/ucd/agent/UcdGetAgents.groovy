/**
 * This action gets a list of agent objects.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

class UcdGetAgents extends UcAdfAction {
	// Action properties.
	/** Flag that indicates to get active agents only. Default is true. */
	Boolean active = true
	
	/** (Optional) If specified then get agent with names that match this regular expression. */
	String match = ""
	
	/** (Optional) Get agents matching a tag name. */
	String tag = ""
	
	/**
	 * Runs the action.
	 * @return Returns the list of agent objects.
	 */
	public List<UcdAgent> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdAgent> ucdReturnAgents = []
		
		WebTarget target 
		
		if (tag) {
			// Validate the agent tag exists and get the tag ID.
			UcdTag tag = actionsRunner.runAction([
				action: UcdGetAgentTag.getSimpleName(),
				actionInfo: false,
				tag: tag,
				failIfNotFound: true
			])
			
			target = ucdSession.getUcdWebTarget().path("/rest/agent/all/tag/{tagId}")
				.resolveTemplate("tagId", tag.getId())
		} else {
			target = ucdSession.getUcdWebTarget().path("/cli/agentCLI")
				.queryParam("active", active)
		}

		// Get the list of agents.			
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			List<UcdAgent> ucdAgents = target.request().get(new GenericType<List<UcdAgent>>(){})
				
			if (match) {
				for (ucdAgent in ucdAgents) {
					if (ucdAgent.getName() ==~ match) {
						ucdReturnAgents.add(ucdAgent)
					}
				}
			} else {
				ucdReturnAgents = ucdAgents
			}
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		return ucdReturnAgents
	}	
}
