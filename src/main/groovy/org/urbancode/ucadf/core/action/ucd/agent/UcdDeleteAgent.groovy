/**
 * This action deletes an agent.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdDeleteAgent extends UcAdfAction {
	// Action properties.
	/** The agent name or ID. */
	String agent
	
	/** The flag that indicates fail if the agent is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true

	/**
	 * Runs the action.	
	 * @return True if the agent was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean deleted = false
		
		if (!commit) {
			logInfo("Would delete agent [$agent].")
		} else {
			logInfo("Delete agent [$agent].")
		
			WebTarget target = ucdSession.getUcdWebTarget()
				.path("/cli/agentCLI")
				.queryParam("agent", agent)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logInfo("Agent [$agent] deleted.")
				deleted = true
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logInfo(errMsg)
				if (response.getStatus() != 404 || failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		}
		
		return deleted
	}
}
