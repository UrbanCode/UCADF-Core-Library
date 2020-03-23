package org.urbancode.ucadf.core.action.ucd.group

import org.urbancode.ucadf.core.action.integration.ldap.UcdGetLdapManager
import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeam
import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeams
import org.urbancode.ucadf.core.action.ucd.user.UcdGetUser
import org.urbancode.ucadf.core.action.ucd.user.UcdImportLdapUsers
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.integration.ldap.LdapManager
import org.urbancode.ucadf.core.integration.ldap.model.LdapGroupResult
import org.urbancode.ucadf.core.integration.ldap.model.LdapGroupUsersResult
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup
import org.urbancode.ucadf.core.model.ucd.role.UcdRoleMapping
import org.urbancode.ucadf.core.model.ucd.team.UcdTeam
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

// Synchronizes the LDAP group members.
// May provide authorizationRealm, authenticationRealm, and connectionPassword or ldapManager (for efficiency if called many times).
class UcdSyncLdapGroupMembers extends UcAdfAction {
	// Action properties.
	/** The authentication realm name or ID. */
	String authenticationRealm
	
	/** The authorization realm name or ID. */
	String authorizationRealm
	
	/** The connection password. */
	UcAdfSecureString connectionPassword = new UcAdfSecureString()

	/** (Optional) The name of a group to synchronize. */
	String group = ""
	
	/** (Optional) The regular expression used to match team names. */
	String teamRegex = ""

	/** The maximum number of users that can be in a group that is synchronized. (Avoids synchronizing very large groups.) */
	Long maxSyncUsers = 200
		
	// Private properties.
	private LdapManager ldapManager
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Initialize the LDAP manager.
		ldapManager = actionsRunner.runAction([
			action: UcdGetLdapManager.getSimpleName(),
			authenticationRealm: authenticationRealm,
			authorizationRealm: authorizationRealm,
			connectionPassword: connectionPassword.toString()
		])
		
		if (group) {
			// Synchronize a single group.
			UcdGroup ucdGroup = actionsRunner.runAction([
				action: UcdGetGroup.getSimpleName(),
				actionInfo: false,
				group: group,
				failIfNotFound: true
			])
			
			syncGroupMembers(ucdGroup)
		} else {
			// Synchronize all groups.
			syncAllGroupMembers()
		}
	}	
	
	// Synchronize all UrbanCode group members with LDAP group members.
	public syncAllGroupMembers() {
		logVerbose("Synchronizing all UrbanCode group members. teamRegex [$teamRegex].")

		// Get the names of the groups associated with UrbanCode team roles.
		Set ucdTeamGroupNames = new TreeSet()
		List<UcdTeam> ucdTeams = actionsRunner.runAction([
			action: UcdGetTeams.getSimpleName()
		])

		for (UcdTeam ucdTeamResult in ucdTeams) {
			if (teamRegex && !(ucdTeamResult.getName() ==~ /$teamRegex/)) {
				continue
			}
			
			UcdTeam ucdTeam = actionsRunner.runAction([
				action: UcdGetTeam.getSimpleName(),
				team: ucdTeamResult.getId(),
				failIfNotFound: true
			])

			println "Getting team [${ucdTeam.getName()}] group names."
	        for (UcdRoleMapping ucdRoleMapping in ucdTeam.getRoleMappings()) {
	        	if (ucdRoleMapping.getGroup()) {
					ucdTeamGroupNames.add(ucdRoleMapping.getGroup().getName())
	            }
	        }
		}

		// Process each of the group names.
		for (groupName in ucdTeamGroupNames) {
			if (groupName ==~ /^P_.*/) {
				logVerbose("Skipping synchronizing group [$groupName] that has a P_ prefix and that can be large.")
				continue
			}
		
			UcdGroup ucdGroup = actionsRunner.runAction([
				action: UcdGetGroup.getSimpleName(),
				group: groupName,
				failIfNotFound: true
			])

			// Synchronize the group members.
			syncGroupMembers(ucdGroup)
		}
	}
	
	// Synchronize an UrbanCode group's members with the LDAP group members.
	public syncGroupMembers(final UcdGroup ucdGroup) {
		String groupName = ucdGroup.getName()
		String groupId = ucdGroup.getId()

		logVerbose("Synchronizing UrbanCode group [$groupName] members.")
		
		// Collect the LDAP group user names.
		Set ldapUserSet = new HashSet()
		List<LdapGroupResult> ldapGroupResults = ldapManager.getGroups(groupName, 1)
		if (ldapGroupResults && ldapGroupResults.size() > 0) {
			List<LdapGroupUsersResult> ldapGroupUsersResults = ldapManager.getGroupUsers(groupName, 1)
			if (ldapGroupUsersResults && ldapGroupUsersResults.size() > 0) {
				for (ldapGroupMember in ldapGroupUsersResults[0].getMembers()) {
					ldapUserSet.add(ldapGroupMember.toUpperCase())
				}
			}
		
			// Collect the UrbanCode group user names.
			Map<String, String> ucdUserMap = new HashMap()
			
			List<UcdUser> ucdGroupMembers = actionsRunner.runAction([
				action: UcdGetGroupMembers.getSimpleName(),
				actionInfo: false,
				group: ucdGroup.getId()
			])

			for (UcdUser ucdUser in ucdGroupMembers) {
				ucdUserMap.put(ucdUser.getName().toUpperCase(), ucdUser.getId())
			}
	
			// Don't synchronize large groups.
			if (ldapUserSet.size() > maxSyncUsers || ucdUserMap.size() > maxSyncUsers) {
				println "Skipping large group [$groupName]."
			} else {
				// Add users to UrbanCode group.			
				for (ldapUserName in ldapUserSet) {
					if (!ucdUserMap.get(ldapUserName)) {
						UcdUser ucdUser = actionsRunner.runAction([
							action: UcdGetUser.getSimpleName(),
							actionInfo: false,
							user: ldapUserName,
							failIfNotFound: false
						])
			
						// Import the user if they aren't found in the UrbanCode instance.
						if (!ucdUser) {
							if (ldapManager.getUsers(ldapUserName, 1).size() < 1) {
								println "  Unable to add user [$ldapUserName] that was found in LDAP group [$groupName] but can't be found by the LDAP user search."
							} else {
								actionsRunner.runAction([
									action: UcdImportLdapUsers.getSimpleName(),
									authenticationRealm: authenticationRealm,
									authorizationRealm: authorizationRealm,
									connectionPassword: connectionPassword,
									users: [ ldapUserName ]
								])
								
								ucdUser = actionsRunner.runAction([
									action: UcdGetUser.getSimpleName(),
									actionInfo: false,
									user: ldapUserName,
									failIfNotFound: false
								])
	
							}
						}
						
						if (ucdUser) {
							actionsRunner.runAction([
								action: UcdAddGroupMember.getSimpleName(),
								group: ucdGroup.getId(),
								user: ucdUser.getId()
							])
						}
					}
				}
				
				// Remove users from UrbanCode group.			
				for (ucdUserName in ucdUserMap.keySet()) {
					if (!ldapUserSet.contains(ucdUserName)) {
						UcdUser ucdUser = actionsRunner.runAction([
							action: UcdGetUser.getSimpleName(),
							actionInfo: false,
							user: ucdUserMap[ ucdUserName ],
							failIfNotFound: false
						])
						
						if (ucdUser) {
							actionsRunner.runAction([
								action: UcdRemoveGroupMember.getSimpleName(),
								group: ucdGroup.getId(),
								user: ucdUser.getId()
							])
						}
					}
				}
			}
		}
	}
}
