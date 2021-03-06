/**
 * This action removes an application from teams/subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtype
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdRemoveApplicationFromTeams extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The list of teams/subtypes. */
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
					
			logVerbose("Remove application [$application] from team [$team] subtype [$subtype].")
	
			// Replaces the subtype IDs in the team mappings by looking up the ID using the subtype.
			String subtypeName = subtype
			if (subtype && UcdObject.isUUID(subtype)) {
				UcdSecuritySubtype ucdSecuritySubtype = actionsRunner.runAction([
					action: UcdGetSecuritySubtype.getSimpleName(),
					actionInfo: false,
					actionVerbose: false,
					subtype: subtype
				])
	
				subtypeName = ucdSecuritySubtype.getName()
			}

			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/teams")
				.queryParam("application", application)
				.queryParam("team", team)
				.queryParam("type", subtypeName)
			logDebug("target=$target")

			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logVerbose("Application [$application] removed from team [$team] subtype [$subtype].")
	        } else if (response.getStatus() == 404) {
	            logVerbose("Application [$application] team [$team] subtype [$subtype] not found.")
			} else if (response.getStatus() == 500) {
				logVerbose("Ignoring status ${response.getStatus()} and assuming application [$application] team [$team] subtype [$subtype] not found.")
			} else {
	            throw new UcAdfInvalidValueException(response)
			}
		}
	}
}
