/**
 * This action gets a list of agent relays.
 */
package org.urbancode.ucadf.core.action.ucd.agentRelay

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agentRelay.UcdAgentRelay

class UcdGetAgentRelays extends UcAdfAction {
	// Action properties.
	/** (Optional) If specified then get agent relays with names that match this regular expression. */
	String match = ""

	/**
	 * Runs the action.
	 * @return The list of agent relay objects.
	 */
	public List<UcdAgentRelay> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdAgentRelay> ucdReturnAgentRelays = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/relay")

		List<UcdAgentRelay> ucdAgentRelays = target.request().get(new GenericType<List<UcdAgentRelay>>(){})
		
		if (match) {
			for (ucdAgentRelay in ucdAgentRelays) {
				if (ucdAgentRelay.getName() ==~ match) {
					ucdReturnAgentRelays.add(ucdAgentRelay)
				}
			}
		} else {
			ucdReturnAgentRelays = ucdAgentRelays
		}
		
		return ucdReturnAgentRelays
	}	
}
