/**
 * This action gets a random agent from an agent pool.
 */
package org.urbancode.ucadf.core.action.ucd.agentPool

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.agentPool.UcdAgentPool
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

// Get a random agent from an agent pool.
class UcdGetPoolAgent extends UcAdfAction {
	// Action properties.
	/** The pool name or ID. */
	String pool

	/**
	 * Runs the action.
	 * @return The agent object.	
	 */
	public UcdAgent run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Get an agent from pool [$pool].")
		
		// Get information about the pool.
		UcdAgentPool agentPool = actionsRunner.runAction([
			action: UcdGetAgentPool.getSimpleName(),
			actionInfo: false,
			pool: pool
		])

		if (agentPool.getAgents().size() < 1) {
			throw new UcAdfInvalidValueException("No agents found in [$pool] pool.")
		}

		UcdAgent ucdAgent = agentPool.getAgents()[0]
		
		logVerbose("Found agent [${ucdAgent.getName()}] in pool [$pool].")

		return ucdAgent
	}	
}
