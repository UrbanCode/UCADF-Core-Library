/**
 * This action determines if a user has an environment team role.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeam
import org.urbancode.ucadf.core.action.ucd.user.UcdIsUserTeamMember
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam

class UcdUserHasEnvironmentTeamRole extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/** The role name or ID. */
	String role
	
	/** The user name or ID. */
	String user
	
	/**
	 * Runs the action.	
	 * @return True if the user has the specified role.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean hasRole = false

		logInfo("Determine if user [$user] has role [$role] for application [$application] environment [$environment].")
		
		// Get the environment information.
		UcdEnvironment ucdEnvironment = actionsRunner.runAction([
			action: UcdGetEnvironment.getSimpleName(),
			application: application,
			environment: environment,
			withDetails: true
		])
		
		// Look at each of the application's teams
		UcdExtendedSecurity extendedSecurity = ucdEnvironment.getExtendedSecurity()
		for (team in extendedSecurity.getTeams()) {
			UcdTeam ucdTeam = actionsRunner.runAction([
				action: UcdGetTeam.getSimpleName(),
				team: team.getTeamName(),
				failIfNotFound: true
			])

			// Determine if the user is a member of the team role.
			hasRole = actionsRunner.runAction([
				action: UcdIsUserTeamMember.getSimpleName(),
				team: ucdTeam.getName(),
				user: user,
				role: role,
				directMember: true,
				groupMember: true
			])
		
			if (hasRole) {
				break
			}
		}

		logInfo("Has role [$hasRole].")
				
		return hasRole
	}
}
