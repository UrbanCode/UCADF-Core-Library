/**
 * This action gets a team.
 */
package org.urbancode.ucadf.core.action.ucd.team

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam

class UcdGetTeam extends UcAdfAction {
	// Action properties.
	/** The team name or ID. */	
	String team
	
	/** The flag that indicates fail if the team is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The team object.
	 */
	@Override
	public UcdTeam run() {
		// Validate the action properties.
		validatePropsExist()

		UcdTeam ucdTeam
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/team/info")
			.queryParam("team", team)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdTeam = response.readEntity(UcdTeam.class)
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if ((response.getStatus() != 403 && response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return ucdTeam
	}
}
