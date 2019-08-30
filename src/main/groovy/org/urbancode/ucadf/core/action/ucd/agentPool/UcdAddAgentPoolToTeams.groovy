/**
 * This action adds the agent pool to teams.
 */
package org.urbancode.ucadf.core.action.ucd.agentPool

import javax.ws.rs.client.Entity
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

class UcdAddAgentPoolToTeams extends UcAdfAction {
	// Action properties.
	/** The agent pool name or ID . */
	String pool
	
	/** The list of teams/subtypes to add to the pool. */
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
					
			logInfo("Add agent pool [$pool] to team [$team]subtype [$subtype].")
	
			// Get the subtype ID.
			String subtypeId = subtype
			if (subtype && !UcdObject.isUUID(subtype)) {
				UcdSecuritySubtype ucdSecuritySubtype = actionsRunner.runAction([
					action: UcdGetSecuritySubtype.getSimpleName(),
					actionInfo: false,
					type: UcdSecurityTypeEnum.AGENTPOOL,
					subtype: subtype
				])

				subtypeId = ucdSecuritySubtype.getId()
			}
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentPool/teams")
				.queryParam("agentPool", pool)
				.queryParam("team", team)
				.queryParam("type", subtypeId)
			logDebug("target=$target")

			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
			if (response.getStatus() == 204) {
				logInfo("Agent pool [$pool] added to team [$team]subtype [$subtype].")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
