/**
 * This action generats a security report about a team or teams.
 */
package org.urbancode.ucadf.core.action.ucadf.security

import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeam
import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeams
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.role.UcdRoleMapping
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam

// If a specific team name is provided then that is reported, otherwise the regular expression is used.
class UcAdfGenerateSecurityReport extends UcAdfAction {
	// Action properties.
	/** (Optional) The name of a team. */
	String team = ""
	
	/** (Optional) If specified then gets teams with names that match this regular expression. */
	String match = ""
	
	/** The list of roles to report. */
	List<String> roles

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Since array variables aren't replaced at YAML evaluation time, must replace the variables in each role in the list.
		List<String> derivedRoles = []
		roles.each {
			derivedRoles.add(actionsRunner.replaceVariablesInText(it))
		}
		
		// Generate the security report.
		logInfo("Generating Team Security Report.")
		List<UcdTeam> ucdTeams = actionsRunner.runAction([
			action: UcdGetTeams.getSimpleName()
		])
		
		for (UcdTeam ucdTeam in ucdTeams) {
			if ((team && ucdTeam.getName() == team) || (!team && ucdTeam.getName() ==~ /${match}/)) {
				// Get the team details.
				UcdTeam ucdTeamInfo = actionsRunner.runAction([
					action: UcdGetTeam.getSimpleName(),
					actionInfo: false,
					team: ucdTeam.getName(),
					failIfNotFound: true
				])
				
				println "\n--- Team [${ucdTeamInfo.getName()}] ---"
				
				for (UcdRoleMapping roleMapping in ucdTeamInfo.getRoleMappings()) {
					// Print groups with role.
					if (roleMapping.getGroup() && derivedRoles.contains(roleMapping.getRole().getName())) {
						println "Role [${roleMapping.getRole().getName()}] group [${roleMapping.getGroup().getName()}]"
					}
					
					// Print users with role.
					if (roleMapping.getUser() && derivedRoles.contains(roleMapping.getRole().getName())) {
						println "Role [${roleMapping.getRole().getName()}] user [${roleMapping.getUser().getName()}]"
					}
				}
			}
		}
	}
}
