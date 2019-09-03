/**
 * This action removes an agent from the specified teams/subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.agent

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

class UcdRemoveAgentFromTeams extends UcAdfAction {
	// Action properties.
	/** The agent name or ID. */
	String agent
	
	/** The list of teams/subtypes. */
	List<UcdTeamSecurity> teams

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		for (teamSecurityType in teams) {
			String team = teamSecurityType.getTeam()
			String subtype = teamSecurityType.getSubtype()
					
			logInfo("Remove agent [$agent] from team [$team] subtype [$subtype].")
	
			// Get the subtype ID.
			String subtypeId = subtype
			if (subtype && !UcdObject.isUUID(subtype)) {
				UcdSecuritySubtype ucdSecuritySubtype = actionsRunner.runAction([
					action: UcdGetSecuritySubtype.getSimpleName(),
					actionInfo: false,
					type: UcdSecurityTypeEnum.AGENT,
					subtype: subtype
				])

				subtypeId = ucdSecuritySubtype.getId()
			}

			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentCLI/teams")
				.queryParam("agent", agent)
				.queryParam("team", team)
				.queryParam("type", subtypeId)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logInfo("Agent [$agent] removed from team [$team]subtype [$subtype].")
	        } else if (response.getStatus() == 404) {
	            logInfo("Agent [$agent] team [$team]subtype [$subtype] not found.")
			} else if (response.getStatus() == 500) {
				logInfo("Ignoring status ${response.getStatus()} and assuming agent [$agent] team [$team]subtype [$subtype] not found.")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
