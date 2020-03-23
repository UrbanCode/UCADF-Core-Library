/**
 * This action deletes an agent pool.
 */
package org.urbancode.ucadf.core.action.ucd.agentPool

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdDeleteAgentPool extends UcAdfAction {
	// Action properties.
	/** The agent pool name or ID. */
	String pool
	
	/** The flag that indicates fail if the agent pool is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true

	/**
	 * Runs the action.	
	 * @return True if the agent pool was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		logVerbose("Delete agent pool [$pool]")
		
		if (!commit) {
			logVerbose("Would delete agent pool [$pool].")
		} else {
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentPool/deleteAgentPool")
				.queryParam("pool", pool)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logVerbose("Agent pool [$pool] deleted.")
				deleted = true
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() != 404 || failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}
		
		return deleted
	}
}
