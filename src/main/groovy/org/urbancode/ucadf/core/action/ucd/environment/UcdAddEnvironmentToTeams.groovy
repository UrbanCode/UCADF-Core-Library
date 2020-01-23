/**
 * This action adds an environment to teams/subtypes.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.exception.UcdNotFoundException
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

class UcdAddEnvironmentToTeams extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/** The list of teams/subtypes. */
	List<UcdTeamSecurity> teams
	
	/** If true then any extra teams/subtypes are removed. Default is false. */
	Boolean removeOthers = false

	/** The flag that indicates fail if the environment is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		UcdEnvironment ucdEnvironment = actionsRunner.runAction([
			action: UcdGetEnvironment.getSimpleName(),
			application: application,
			environment: environment,
			failIfNotFound: failIfNotFound
		])
		
		if (ucdEnvironment) {
			for (teamSecurity in teams) {
				addEnvironmentToTeam(
					teamSecurity.getTeam(),
					teamSecurity.getSubtype()
				)
			}
	
			// Remove the environment from extra teams.
			if (teams && removeOthers) {
				removeEnvironmentFromExtraTeams(
					teams
				)
			}
		} else {
			if (failIfNotFound) {
				throw new UcdNotFoundException("Environment [$environment] not found.")
			}
		}
	}
	
	// Add application environment to a team.
	public addEnvironmentToTeam(
		final String team,
		final String subtype) {
				
		logVerbose("Adding application [$application] environment [$environment] to team [$team] type [$subtype].")

		// Add the environment to the team.
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/teams")
			.queryParam("application", application)
			.queryParam("environment", environment)
			.queryParam("team", team)
			.queryParam("type", subtype)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
		if (response.getStatus() != 204) {
            throw new UcdInvalidValueException(response)
		}
	}
	
	// Remove the environment from extra teams.
	public removeEnvironmentFromExtraTeams(
		final List<UcdTeamSecurity> keepTeams) {

		// Get the environment information.		
		UcdEnvironment derivedEnvironment = actionsRunner.runAction([
			action: UcdGetEnvironment.getSimpleName(),
			application: application,
			environment: environment,
			withDetails: true
		])

		if (!derivedEnvironment) {
			throw new UcdInvalidValueException("Application [$application] environment [$environment] not found.")
		}

		for (team in derivedEnvironment.getExtendedSecurity().getTeams()) {
			Boolean removeTeam = true
			for (keepTeamSecurity in keepTeams) {
				if (team.getTeamName() == keepTeamSecurity.getTeam() &&
					((!team.getSubtypeName() && !keepTeamSecurity.getSubtype()) || (team.getSubtypeName() == keepTeamSecurity.getSubtype()))) {
					
					removeTeam = false
					break
				}
			}
			
			if (removeTeam) {
				logVerbose("Removing team [${team.getTeamName()}] role [${team.getSubtypeName()}].")
				
				WebTarget removeTargetWithParams = ucdSession.getUcdWebTarget().path("/cli/environment/teams")
					.queryParam("application", application)
					.queryParam("environment", environment)
					.queryParam("team", team.getTeamName())
					
				if (team.getSubtypeName()) {
					removeTargetWithParams = removeTargetWithParams.queryParam("type", team.getSubtypeName())
				} else {
					removeTargetWithParams = removeTargetWithParams.queryParam("type", "")
				}
				logDebug("removeTargetWithParams=$removeTargetWithParams")
				
				Response response = removeTargetWithParams.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() != 204) {
					throw new UcdInvalidValueException(response)
				}
			}
		}
	}
}
