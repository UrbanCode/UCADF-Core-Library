/**
 * This action creates a team.
 */
package org.urbancode.ucadf.core.action.ucd.team

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdCreateTeam extends UcAdfAction {
	/** The team name. */
	String name

	/** (Optional) The description. */	
	String description = ""
	
	/** The flag that indicates fail if the team already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the team was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean created = false
		
		logVerbose("Creating team [$name].")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/team/create")
			.queryParam("team", name)
			.queryParam("description", description)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.text(""))
		if (response.getStatus() == 200) {
			logVerbose("Team created.")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return created
	}	
}
