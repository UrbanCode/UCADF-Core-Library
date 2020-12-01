/**
 * This action adds an agent to teams.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtype
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdAddAgentToTeams extends UcAdfAction {
	// Action properties.
	/** The agent name or ID. */
	String agent
	
	/** The list of teams/subtypes. */
	List<UcdTeamSecurity> teams
	
	/** The flag to remove any extra teams/subtypes. Default is false. */
	Boolean removeOthers = false

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
					
			logVerbose("Add agent [$agent] to team [$team] subtype [$subtype].")

			// Get the subtype name.
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
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentCLI/teams")
				.queryParam("agent", agent)
				.queryParam("team", team)
				.queryParam("type", subtypeName)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
			if (response.getStatus() == 204) {
				logVerbose("Agent [$agent] added to team [$team] subtype [$subtype].")
			} else {
	            throw new UcAdfInvalidValueException(response)
			}
		}

		// Remove from extra teams.		
		if (removeOthers) {
			removeOthers()
		}
	}

	// Remove the agent from extra teams.
	private removeOthers() {
		// Get the agent information.
		UcdAgent ucdAgent = actionsRunner.runAction([
			action: UcdGetAgent.getSimpleName(),
			actionInfo: false,
			agent: agent,
			failIfNotFound: true
		])

		for (team in ucdAgent.getExtendedSecurity().getTeams()) {
			Boolean removeTeam = true
			for (keepTeamSecurityType in teams) {
				if (team.getTeamName() == keepTeamSecurityType.getTeam() &&
					((!team.getSubtypeName() && !keepTeamSecurityType.getSubtype()) || (team.getSubtypeName() == keepTeamSecurityType.getSubtype()))) {
					
					removeTeam = false
					break
				}
			}
			
			if (removeTeam) {
				logVerbose("Removing team [${team.getTeamName()}] type [${team.getSubtypeName()}] from agent [$agent].")

				WebTarget removeTargetWithParams = ucdSession.getUcdWebTarget().path("/cli/agentCLI/teams")
					.queryParam("agent", agent)
					.queryParam("team", team.getTeamName())
					
				if (team.getSubtypeName()) {
					removeTargetWithParams = removeTargetWithParams.queryParam("type", team.getSubtypeName())
				} else {
					removeTargetWithParams = removeTargetWithParams.queryParam("type", "")
				}
				
				logDebug("removeTargetWithParams=$removeTargetWithParams")
				
				Response response = removeTargetWithParams.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 204) {
					throw new UcAdfInvalidValueException(response)
				}
			}
		}
	}
}
