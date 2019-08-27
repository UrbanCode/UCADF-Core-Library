/**
 * This action gets an agent relay.
 */
package org.urbancode.ucadf.core.action.ucd.agentRelay

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agentRelay.UcdAgentRelay
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetAgentRelay extends UcAdfAction {
	// Action properties.
	/** The agent relay name or ID. */
	String relay
	
	/** The flag that indicates fail if the agent relay is not found. Default is true. */
	Boolean failIfNotFound = true

	/**
	 * Runs the action.
	 * @return the agent relay object.	
	 */
	public UcdAgentRelay run() {
		// Validate the action properties.
		validatePropsExist()

		UcdAgentRelay ucdAgentRelay
		WebTarget target 
		Response response

		// Get it by name first in order to get the ID. This does not return the extended security information.	
		target = ucdSession.getUcdWebTarget().path("/rest/relay")
			.queryParam("relayName", relay)
		logDebug("target=$target")

		response = target.request().get()
		if (response.getStatus() == 200) {
			List<UcdAgentRelay> ucdAgentRelays = response.readEntity(new GenericType<List<UcdAgentRelay>>(){})
			ucdAgentRelay = ucdAgentRelays[0]
			
			// Now get it by ID in order to get all the information included the extended security information.
			target = ucdSession.getUcdWebTarget().path("/rest/relay/{relayName}")
				.resolveTemplate("relayName", ucdAgentRelay.getId())
			logDebug("target=$target")
	
			response = target.request().get()
			if (response.getStatus() == 200) {
				ucdAgentRelay = response.readEntity(UcdAgentRelay.class)
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logInfo(errMsg)
				if (response.getStatus() != 404 || failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		return ucdAgentRelay
	}	
}
