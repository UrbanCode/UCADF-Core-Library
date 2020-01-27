/**
 * This actions gets the users that belong to a team.
 */
package org.urbancode.ucadf.core.action.ucd.team

import org.urbancode.ucadf.core.action.ucd.group.UcdGetGroupMembers
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdGetTeamUsers extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list. */
		LIST,
		
		/** Return as a map having the team name as the key. */
		MAPBYNAME
	}

	// Action properties.
	/** The team name or ID. */
	String team
	
	/** The role name or ID. */
	String role = ""
	
	/** The type of collection to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.LIST
	
	/**
	 * Runs the action.	
	 * @return The specified type of collection of team users.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Get team users and users that are a member of the team groups.
		Map<String, UcdUser> ucdUsersMap = [:]
        
		// Get the team information.
		UcdTeam ucdTeam = actionsRunner.runAction([
			action: UcdGetTeam.getSimpleName(),
			actionInfo: actionInfo,
			team: team,
			failIfNotFound: true
		])
		
        for (ucdRoleMapping in ucdTeam.getRoleMappings()) {
            if (!ucdRoleMapping.getRole() || (role && !role.equals(ucdRoleMapping.getRole().getName()))) {
                continue
            }

			// If the role mapping is to a user then add the user to the map if they don't already exist in the map.            
            if (ucdRoleMapping.getUser()) {
				UcdUser ucdUser = ucdRoleMapping.getUser()
				if (!ucdUsersMap.containsKey(ucdUser.getName())) {
					ucdUsersMap.put(
						ucdUser.getName().toUpperCase(), 
						ucdUser
					)
				}
				continue
            }
			
			// If the role mapping is to a group then add the users that are members of that group.
            if (ucdRoleMapping.getGroup()) {
				List<UcdUser> ucdGroupUsers = actionsRunner.runAction([
					action: UcdGetGroupMembers.getSimpleName(),
					actionInfo: actionInfo,
					group: ucdRoleMapping.getGroup().getName()
				])

				for (ucdUser in ucdGroupUsers) {
					if (!ucdUsersMap.containsKey(ucdUser.getName())) {
						ucdUsersMap.put(
							ucdUser.getName().toUpperCase(),
							ucdUser
						)
					}
				}
            }
        }

		// Return as requested.
		Object teamUsers
		if (ReturnAsEnum.LIST.equals(returnAs)) {
			// Convert the map to a list.
			List<UcdUser> ucdUsers = []
			ucdUsersMap.each { userName, ucdUser ->
				ucdUsers.add(ucdUser)
			}
			teamUsers = ucdUsers
		} else {
			teamUsers = ucdUsersMap
		}
		
		return teamUsers
	}
}
