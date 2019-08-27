/**
 * This action gets an agent pool.
 */
package org.urbancode.ucadf.core.action.ucd.agentPool

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agentPool.UcdAgentPool
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetAgentPool extends UcAdfAction {
	// Action properties.
	/** The agent pool name or ID. */
	String pool
	
	/** The flag that indicates fail if the agent pool is not found. Default is true. */
	Boolean failIfNotFound = true

	/**
	 * Runs the action.	
	 * @return The agent pool object.
	 */
	public UcdAgentPool run() {
		// Validate the action properties.
		validatePropsExist()

		UcdAgentPool ucdAgentPool
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/agent/pool/{pool}")
			.resolveTemplate("pool", pool)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdAgentPool = response.readEntity(UcdAgentPool.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdAgentPool
	}	
}
