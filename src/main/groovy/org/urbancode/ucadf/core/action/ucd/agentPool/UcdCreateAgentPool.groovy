/**
 * This action creates an agent pool.
 */
package org.urbancode.ucadf.core.action.ucd.agentPool

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.agent.UcdGetAgents
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.agentPool.UcdAgentPool
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

import groovy.json.JsonBuilder

class UcdCreateAgentPool extends UcAdfAction {
	// Action properties.
	/** The agent pool name. */
	String name
	
	/** The (Optional) agent pool description. */
	String description = ""

	/** (Optional if tag provided.) The list of agents to be added to the agent pool. */
	List<String> agents = []
	
	/** (Optional if agents provided.) The tag used to look for agents to add to the pool. */
	String tag = ""
	
	/** The flag that indicates fail if the agent pool already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the agent pool was created.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
				
		logVerbose("Create agent pool [$name].")

		// See if the agent pool already exists.
		UcdAgentPool ucdAgentPool = actionsRunner.runAction([
			action: UcdGetAgentPool.getSimpleName(),
			actionInfo: false,
			pool: name,
			failIfNotFound: false
		])

		if (ucdAgentPool) {
			if (failIfExists) {
				throw new UcAdfInvalidValueException("Agent pool [$name] already exists.")
			} else {
				logVerbose("Agent pool [$name] already exists.")
			}
		} else {
			logVerbose("Agent pool [$name] does not exist.")
			
			// Construct a list of agent names to add to the pool.
			List<String> agentNames = []

			// If agents were specfied then add them to the list.
			agentNames.addAll(agents)
			
			// If a tag was specified then look for agents with that tag and add them to the list.			
			if (tag) {
				logVerbose("Finding agents with tag [$tag] to add to pool.")
				List<UcdAgent> ucdAgents = actionsRunner.runAction([
					action: UcdGetAgents.getSimpleName(),
					actionInfo: false,
					tag: tag
				])
				
				if (ucdAgents.size() < 1) {
					throw new UcAdfInvalidValueException("No agents found with tag [$tag].")
				}
			
				ucdAgents.each { 
					agentNames.add(it.name)
				}
			}
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentPool/createAgentPool")
			logDebug("target=$target")

			// Build a custom post body that includes only the required fields.
			Map requestMap = [
				name: name, 
				description: description, 
				agents: agentNames
			]
			
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				logVerbose("Agent pool [$name] created.")
				created = true
			} else {
				throw new UcAdfInvalidValueException(response)
			}
		}
		
		return created
	}
}
