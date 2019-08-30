/**
 * This action removes an environment from teams/subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtype
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdRemoveEnvironmentFromTeams extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
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
					
			logInfo("Remove application [$application] from team [$team]subtype [$subtype].")
			logInfo("Removing application [$application] environment [$environment] from team [$team]subtype [$subtype].")
			
			// Replaces the subtype IDs in the team mappings by looking up the ID using the subtype name.
			String subtypeId = subtype
			if (subtype && !UcdObject.isUUID(subtype)) {
				UcdSecuritySubtype ucdSecuritySubtype = actionsRunner.runAction([
					action: UcdGetSecuritySubtype.getSimpleName(),
					type: UcdSecurityTypeEnum.ENVIRONMENT,
					subtype: subtype
				])
	
				subtypeId = ucdSecuritySubtype.getId()
			}

			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/teams")
				.queryParam("application", application)
				.queryParam("environment", environment)
				.queryParam("team", team)
				.queryParam("type", subtypeId)
			logDebug("target=$target")

			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logInfo("Application [$application] environment [$environment] removed from team [$team]subtype [$subtype].")
	        } else if (response.getStatus() == 404) {
	            logInfo("Application [$application] environment [$environment] team [$team]subtype [$subtype] not found.")
			} else if (response.getStatus() == 500) {
				logInfo("Ignoring status ${response.getStatus()} and assuming application [$application] environment [$environment] team [$team]subtype [$subtype] not found.")
			} else {
	            throw new UcdInvalidValueException(response)
			}
		}
	}
}
