/**
 * This action gets teams.
 */
package org.urbancode.ucadf.core.action.ucd.team

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam

class UcdGetTeams extends UcAdfAction {
	// Action properties.
	/** (Optional) If specified then get teams with names that match this regular expression. */
	String match = ""
	
	/**
	 * Runs the action.	
	 * @return The list of teams.
	 */
	@Override
	public List<UcdTeam> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdTeam> ucdTeams = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/team")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdTeams = response.readEntity(new GenericType<List<UcdTeam>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}

		List<UcdTeam> ucdReturnTeams = []
		
		if (match) {
			for (ucdTeam in ucdTeams) {
				if (ucdTeam.getName() ==~ match) {
					ucdReturnTeams.add(ucdTeam)
				}
			}
		} else {
			ucdReturnTeams = ucdTeams
		}
		
		return ucdReturnTeams
	}
}
