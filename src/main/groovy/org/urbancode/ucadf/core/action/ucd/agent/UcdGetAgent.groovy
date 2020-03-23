/**
 * This action gets an agent object.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdGetAgent extends UcAdfAction {
	// Action properties.
	/** The agent name or ID. */
	String agent
	
	/** The flag that indicates fail if the agent is not found. Default is true. */
	Boolean failIfNotFound = true

	/**
	 * Runs the action.
	 * @return The agent object.	
	 */
	public UcdAgent run() {
		// Validate the action properties.
		validatePropsExist()

		UcdAgent ucdAgent
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentCLI/info")
			.queryParam("agent", agent)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdAgent = response.readEntity(UcdAgent.class)
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return ucdAgent
	}	
}
