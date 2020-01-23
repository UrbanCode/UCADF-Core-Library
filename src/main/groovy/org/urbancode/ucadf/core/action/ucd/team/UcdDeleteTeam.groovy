/**
 * This action deletes a team.
 */
package org.urbancode.ucadf.core.action.ucd.team

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdDeleteTeam extends UcAdfAction {
	/** The team name or ID. */
	String team
	
	/** The flag that indicates fail if the team is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/**
	 * Runs the action.	
	 * @return True if the team was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		if (!commit) {
			logVerbose("Would delete team [$team].")
		} else {
			logVerbose("Deleting team [$team].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/team/{teamName}")
				.resolveTemplate("teamName", team)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.WILDCARD).delete()
			if (response.getStatus() == 200) {
				logVerbose("Team [$team] deleted.")
				deleted = true
	        } else if (response.getStatus() == 404 || response.getStatus() == 500) {
				// Some older versions return a 500.
				logVerbose(response.readEntity(String.class))
				if (failIfNotFound) {
					throw new UcdInvalidValueException("Team not found to delete.")
				}
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
		
		return deleted
	}	
}
