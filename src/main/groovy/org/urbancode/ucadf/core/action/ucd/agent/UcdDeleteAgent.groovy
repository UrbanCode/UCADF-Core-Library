/**
 * This action deletes an agent.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

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
			logVerbose("Would delete agent [$agent].")
		} else {
			logVerbose("Delete agent [$agent].")
		
			WebTarget target = ucdSession.getUcdWebTarget()
				.path("/cli/agentCLI")
				.queryParam("agent", agent)
			logDebug("target=$target")
			
			// Had to add logic to handle concurrency issue discovered in UCD 7.0.1.2.
			final Integer MAXATTEMPTS = 5
			for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				response.bufferEntity()
				
				if (response.getStatus() == 204) {
					logVerbose("Agent [$agent] deleted.")
					deleted = true
					break
				} else if (response.getStatus() == 404) {
					String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
					logVerbose(errMsg)
					if (failIfNotFound) {
						throw new UcAdfInvalidValueException(errMsg)
					}
					break
				} else {
					String responseStr = response.readEntity(String.class)
					logVerbose(responseStr)
					if ((responseStr ==~ /.*bulk manipulation query.*/ || responseStr ==~ /.*another transaction.*/) && iAttempt < MAXATTEMPTS) {
						logVerbose("Attempt $iAttempt failed. Waiting to try again.")
						Thread.sleep(2000)
					} else {
						throw new UcAdfInvalidValueException(response)
					}
				}
			}
		}
		
		return deleted
	}
}
