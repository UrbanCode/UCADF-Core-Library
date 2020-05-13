/**
 * This action copies team members from one team to another.
 */
package org.urbancode.ucadf.core.action.ucd.team

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.role.UcdRoleMapping
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam

// Copies all of the groups and users from one team role to another team role.
class UcdCopyTeamMembers extends UcAdfAction {
	/** The from team name or ID. */
	String fromTeam
	
	/** The from role name or ID. */
	String fromRole
	
	/** The to team name or ID. */
	String toTeam
	
	/** The to role name or ID. */
	String toRole
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Copying team [$fromTeam] role [$fromRole] to team [$toTeam] role [$toRole].")
		
		UcdTeam ucdFromTeamInfo = actionsRunner.runAction([
			action: UcdGetTeam.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			team: fromTeam,
			failIfNotFound: true
		])
		
		UcdTeam ucdToTeamInfo = actionsRunner.runAction([
			action: UcdGetTeam.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			team: toTeam,
			failIfNotFound: true
		])

		List<String> addGroups = []
		List<String> addUsers = []
		
		for (UcdRoleMapping roleMapping in ucdFromTeamInfo.getRoleMappings()) {
			if (roleMapping.getRole() && roleMapping.getRole().getName() == fromRole) {
				if (roleMapping.getGroup() && !ucdToTeamInfo.isGroupTeamMember(toRole, roleMapping.getGroup().getName())) {
					addGroups.add(roleMapping.getGroup().getName())
				}
				
				if (roleMapping.getUser() && !ucdToTeamInfo.isUserTeamMember(toRole, roleMapping.getUser().getName())) {
					addUsers.add(roleMapping.getUser().getName())
				}
			}
		}

		// Add the users and groups to the team.		
		actionsRunner.runAction([
			action: UcdChangeTeamMembers.getSimpleName(),
			actionInfo: false,
			actionVerbose: actionVerbose,
			addUsers: addUsers,
			addGroups: addGroups,
			teamRoles: [
				[
					team: toTeam, 
					role: toRole
				]
			]
		])
	}	
}
