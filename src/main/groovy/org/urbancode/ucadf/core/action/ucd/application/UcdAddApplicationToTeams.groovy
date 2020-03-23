/**
 * This action adds an application to teams/subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtype
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdAddApplicationToTeams extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The list of teams/subtypes to add to the application. */
	List<UcdTeamSecurity> teams
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		for (teamSecurity in teams) {
			String team = teamSecurity.getTeam()
			String subtype = teamSecurity.getSubtype()
			
			logVerbose("Adding application [$application] to team [$team] subtype [$subtype].")
			
			// Get the subtype name required for the API call.
			String subtypeName = subtype
			if (subtype && UcdObject.isUUID(subtype)) {
				UcdSecuritySubtype ucdSecuritySubtype = actionsRunner.runAction([
					action: UcdGetSecuritySubtype.getSimpleName(),
					subtype: subtype
				])
				
				subtypeName = ucdSecuritySubtype.getName()
			}

			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/teams")
				.queryParam("application", application)
				.queryParam("team", team)
				.queryParam("type", subtypeName)
				
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
			if (response.getStatus() == 204) {
				logVerbose("Application [$application] added to team [$team]subtype [$subtype].")
			} else {
				throw new UcAdfInvalidValueException(response)
			}
		}
	}
}
