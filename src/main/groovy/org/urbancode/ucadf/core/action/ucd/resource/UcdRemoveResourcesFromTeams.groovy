/**
 * This action removes resources from teams/subteams.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdRemoveResourcesFromTeams extends UcAdfAction {
	/** The list of resource names or IDs. */
	List<String> resources
	
	/** The list of teams/subtypes. */
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

		for (resource in resources) {
			for (team in teams) {
				removeResourceFromTeam(
					resource,
					team.getTeam(),
					team.getSubtype()
				)
			}
		}
	}

	// Remove a resource from a team.
	public removeResourceFromTeam(
		final String resource,
		final String teamLabel,
		final String resourceRoleLabel) {
		
		logVerbose("Removing resource [$resource] teamLabel [$teamLabel] resourceRoleLabel [$resourceRoleLabel].")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/teams")
			.queryParam("resource", resource)
			.queryParam("team", teamLabel)
			.queryParam("type", resourceRoleLabel ? resourceRoleLabel : "")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() != 204) {
			throw new UcdInvalidValueException(response)
		}
	}
}
