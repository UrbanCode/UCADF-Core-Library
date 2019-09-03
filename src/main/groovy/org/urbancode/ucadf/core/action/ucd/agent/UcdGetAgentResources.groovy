/**
 * This action gets the resources associated with an agent.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdGetAgentResources extends UcAdfAction {
	// Action properties.
	/**
	 * The agent name or UUID.
	 */
	String agent
	
	/**
	 * Runs the action.	
	 * @return Returns the list of resource objects.
	 */
	@Override
	public List<UcdResource> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdResource> ucdResources = []
		
		// Get the agent ID.
		String agentId = agent
		if (agent && !UcdObject.isUUID(agent)) {
			UcdAgent ucdAgent = actionsRunner.runAction([
				action: UcdGetAgent.getSimpleName(),
				actionInfo: false,
				agent: agent
			])
			
			agentId = ucdAgent.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/agent/{agentId}/resources")
			.resolveTemplate("agentId", agentId)
		logDebug("target=$target")
			
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdResources = response.readEntity(new GenericType<List<UcdResource>>(){})
		} else {
			logInfo(response.readEntity(String.class))
			throw new Exception("Status: ${response.getStatus()} Unable to get agent resources. $target")
		}
		
		return ucdResources
	}
}
