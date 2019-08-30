/**
 * This action adds an agent relay to teams.
 */
package org.urbancode.ucadf.core.action.ucd.agentRelay

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecurityTeamMappings
import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeam
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agentRelay.UcdAgentRelay
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

import groovy.json.JsonBuilder

class UcdAddAgentRelayToTeams extends UcAdfAction {
	// Action properties.
	/** The agent relay name or ID. */
	String relay
	
	/** The list of teams/subtypes to add to the agent. */
	List<UcdTeamSecurity> teams
	
	/** If true then any extra teams/subtypes are removed. Default is false. */
	Boolean removeOthers = false

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		UcdAgentRelay ucdAgentRelay = actionsRunner.runAction([
			action: UcdGetAgentRelay.getSimpleName(),
			actionInfo: false,
			relay: relay,
			failIfNotFound: true
		])

		// Get the team mappings.
		List<Map<String, String>> teamMappings = actionsRunner.runAction([
			action: UcdGetSecurityTeamMappings.getSimpleName(),
			actionInfo: false,
			extendedSecurity: ucdAgentRelay.getExtendedSecurity(),
			teams: teams,
			removeOthers: removeOthers
		])
		
		Map requestMap = [
			teamMappings: teamMappings
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=$jsonBuilder")

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/relay/{relayId}")
			.resolveTemplate("relayId", ucdAgentRelay.getId())
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() != 200) {	
			throw new UcdInvalidValueException(response)
		}
	}
}
