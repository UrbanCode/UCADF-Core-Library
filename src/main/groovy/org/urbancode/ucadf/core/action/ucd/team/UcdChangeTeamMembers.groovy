/**
 * This action changes a team's members.
 */
package org.urbancode.ucadf.core.action.ucd.team

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.group.UcdAddLdapGroups
import org.urbancode.ucadf.core.action.ucd.group.UcdGetGroup
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdSecureString
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamRole

class UcdChangeTeamMembers extends UcAdfAction {
	/** The list of team roles. */
	List<UcdTeamRole> teamRoles = []
	
	/** The list of group names or IDs to add. */
	List<String> addGroups = []
	
	/** The list of user names or IDs to add. */
	List<String> addUsers = []
	
	/** The list of group names or IDs to remove. */
	List<String> removeGroups = []
	
	/** The list of user names or IDs to remove. */
	List<String> removeUsers = []

	/** This flag indicates to attempt to auto-add the group if it doesn't exist. */
	Boolean autoAddGroup = false
	
	/** The authoriziation realm to use if auto-adding groups by importing a user. */
	String authorizationRealm = ""

	/** The authentication realm to use if auto-adding groups by importing a user. */
	String authenticationRealm = ""	
	
	/** The connection password. */
	UcdSecureString connectionPassword = new UcdSecureString()

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// TODO: Need to implement remove groups/users.
		if (removeGroups.size() > 0 || removeUsers.size() > 0) {
			throw new UcdInvalidValueException("Remove groups/users has not been implemented yet.")
		}
		
		// Process each team role.
		for (teamRole in teamRoles) {
			String team = teamRole.getTeam()
			String role = teamRole.getRole()
			
			UcdTeam ucdTeam = actionsRunner.runAction([
				action: UcdGetTeam.getSimpleName(),
				actionInfo: false,
				team: team
			])
			
			if (!ucdTeam) {
				throw new UcdInvalidValueException("Team not found.")
			}

			// Add the groups to the team role.		
			// Validate group names to force import of the LDAP groups.
			validateGroupNames(addGroups)

			for (groupName in addGroups) {
				groupName = groupName.trim()
				if (!ucdTeam.isGroupTeamMember(role, groupName)) {
					addGroupToTeam(team, role, groupName)
				}
			}

			// Add the users to the team role.
			for (userName in addUsers) {
				userName = userName.trim()
				if (!ucdTeam.isUserTeamMember(role, userName)) {
					addUserToTeam(team, role, userName)
				}
			}
		}
	}	

	// Add a group to a role for a team.
	public addGroupToTeam(
		final String team, 
		final String role, 
		final String group) {
		
		logInfo("Adding group [$group] to team [$team] role [$role].")
        
        // Handle difference between UCD 6.1 and UCD 6.2 API
        String roleNameParam
        if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_61)) {
            roleNameParam = "type"
        } else {
            roleNameParam = "role"
        }
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/teamsecurity/groups")
			.queryParam("group", group)
			.queryParam("team", team)
			.queryParam(roleNameParam, role)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.text(""))
		if (response.getStatus() == 204) {
			logInfo("Group added to team.")
		} else {
            throw new UcdInvalidValueException(response)
		}
	}
	
	// Add a user to a team.
	public addUserToTeam(
		final String team, 
		final String role, 
		final String user) {
		
		logInfo("Adding user [$user] to team [$team] role [$role].")
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/teamsecurity/users")
			.queryParam("user", user)
			.queryParam("team", team)
			.queryParam("role", role)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.text(""))
		if (response.getStatus() == 204) {
			logInfo("User added to team.")
		} else {
            throw new UcdInvalidValueException(response)
		}
	}
	
	// Validate a list of group names.
	public validateGroupNames(final List<String> groupNames) {
		if (groupNames) {
			for (groupName in groupNames) {
				UcdGroup ucdGroup = actionsRunner.runAction([
					action: UcdGetGroup.getSimpleName(),
					actionInfo: false,
					group: groupName,
					failIfNotFound: false
				])

				if (!ucdGroup && autoAddGroup) {
					// TODO: This needs to be able to handle things other than LDAP.
					// Attempt to add the missing LDAP group.
					logInfo("Attempting to add missing LDAP group [$groupName].")
					actionsRunner.runAction([
						action: UcdAddLdapGroups.getSimpleName(),
						actionInfo: false,
						authorizationRealm: authorizationRealm,
						authenticationRealm: authenticationRealm,
						connectionPassword: connectionPassword,
						groups: [ groupName ]
					])
				}
				
				// Validate group exists after adding.
				ucdGroup = actionsRunner.runAction([
					action: UcdGetGroup.getSimpleName(),
					actionInfo: false,
					group: groupName,
					failIfNotFound: false
				])
				
				if (!ucdGroup) {
					throw new UcdInvalidValueException("Group [$groupName] is not valid.")
				}
			}
		}
	}
}
