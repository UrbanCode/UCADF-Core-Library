/**
 * This action gets a list of agent pools.
 */
package org.urbancode.ucadf.core.action.ucd.agentPool

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agentPool.UcdAgentPool
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdGetAgentPools extends UcAdfAction {
	// Action properties.
	/** If true then get active pools. Default is true*/
	Boolean active = true
	
	/** If true then get inactive pools. Default is false. */
	Boolean inactive = false
	
	/** (Optional) If specified then gets agent pools with names that match this regular expression. */
	String match = ""
	
	/**
	 * Runs the actions.	
	 * @return The list of agent pools.
	 */
	public List<UcdAgentPool> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdAgentPool> ucdReturnAgentPools = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentPool")

		if (active) {		
			target = target.queryParam("active", active)
		}

		if (inactive) {		
			target = target.queryParam("inactive", inactive)
		}
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			List<UcdAgentPool> ucdAgentPools = response.readEntity(new GenericType<List<UcdAgentPool>>(){})
			
			if (match) {
				for (ucdAgentPool in ucdAgentPools) {
					if (ucdAgentPool.getName() ==~ match) {
						ucdReturnAgentPools.add(ucdAgentPool)
					}
				}
			} else {
				ucdReturnAgentPools = ucdAgentPools
			}
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		return ucdReturnAgentPools
	}	
}
