/**
 * This action determines if a user is a team member.
 */
package org.urbancode.ucadf.core.action.ucd.user

import org.urbancode.ucadf.core.action.ucd.group.UcdGetGroupMembers
import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeam
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.role.UcdRoleMapping
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdIsUserTeamMember extends UcAdfAction {
	// Action properties.
	/** The team name or ID. */
	String team
	
	/** The user name or ID. */
	String user
	
	/** The role name. */
	String role
	
	/** Flag that indicates to check if the user is a direct member. */
	Boolean directMember = true

	/** Flag that indicates to check if the user is an indirect member via a group. */	
	Boolean groupMember = true

	private UcdTeam ucdTeam
		
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// See if the specified user is a member of any group associated with the specified team role.
		ucdTeam = actionsRunner.runAction([
			action: UcdGetTeam.getSimpleName(),
			team: team,
			failIfNotFound: true
		])
		
		return ((directMember && ucdTeam.isUserTeamMember(role, user)) || (groupMember && isUserTeamGroupMember()))
	}

	// Determine if the user is a member of any of the team's groups.	
	public Boolean isUserTeamGroupMember() {
		Boolean isMember = false
		
		List<UcdRoleMapping> roleMappings = ucdTeam.getRoleMappings()
		for (roleMapping in roleMappings) {
			if (roleMapping.getGroup() && (roleMapping.getRole().getName().equals(role) || roleMapping.getRole().getId().equals(role))) {
				List<UcdUser> ucdUsers = actionsRunner.runAction([
					action: UcdGetGroupMembers.getSimpleName(),
					group: roleMapping.getGroup().getName()
				])

				if (ucdUsers.find { (it.getName().equals(user) || it.getId().equals(user)) }) {
					isMember = true
					break
				}
			}
		}
		
		return isMember
	}
}
