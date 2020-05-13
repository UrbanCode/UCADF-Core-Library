/**
 * This action adds LDAP groups. This requires finding a user that's a member of the LDAP group and importing them.
 */
package org.urbancode.ucadf.core.action.ucd.group

import org.urbancode.ucadf.core.action.integration.ldap.UcdGetLdapManager
import org.urbancode.ucadf.core.action.ucd.user.UcdGetUser
import org.urbancode.ucadf.core.action.ucd.user.UcdImportLdapUsers
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.integration.ldap.LdapManager
import org.urbancode.ucadf.core.integration.ldap.model.LdapGroupResult
import org.urbancode.ucadf.core.integration.ldap.model.LdapGroupUsersResult
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

// Add LDAP groups.
// May provide authorizationRealm, authenticationRealm, and connectionPassword or ldapManager (for efficiency if called many times).
class UcdAddLdapGroups extends UcAdfAction {
	// Action properties.
	/** The authentication realm name or ID. */
	String authenticationRealm
	
	/** The authorization realm name or ID. */
	String authorizationRealm
	
	/** The connection password. */
	UcAdfSecureString connectionPassword = new UcAdfSecureString()
	
	/** The list of group names or IDs. */
	List<String> groups
	
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
			actionInfo: false,
			actionVerbose: false,
			authenticationRealm: authenticationRealm,
			authorizationRealm: authorizationRealm,
			connectionPassword: connectionPassword.toString()
		])

		// Add the LDAP groups.
        addLdapGroups(groups*.trim())
	}	
	
	// Adds an LDAP group by finding a user of the group to import.
	public addLdapGroups(final List<String> groupNames) {
		logVerbose("Adding LDAP groups to authentication realm [${authenticationRealm}].")
	
		for (groupName in groupNames) {
			UcdGroup ucdGroup = actionsRunner.runAction([
				action: UcdGetGroup.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				group: groupName,
				failIfNotFound: false
			])
			
			if (ucdGroup) {
				logVerbose("Group [$groupName] already exists.")
				continue
			}
			
			logVerbose("Attempting to automatically add group [$groupName].")
			List<LdapGroupResult> ldapGroupResults = ldapManager.getGroups(groupName, 1)
			if (ldapGroupResults && ldapGroupResults.size() > 0) {
				logVerbose("Found group [$groupName] in LDAP. Looking for a user ID to use to import group.")
				List<LdapGroupUsersResult> ldapGroupUsersResults = ldapManager.getGroupUsers(groupName, 1)
				if (ldapGroupUsersResults && ldapGroupUsersResults.size() > 0 && ldapGroupUsersResults[0].getMembers().size() > 0) {
					logVerbose("Group [$groupName] has ${ldapGroupUsersResults[0].getMembers().size()} members.")
					
					Iterator memberIterator = ldapGroupUsersResults[0].getMembers().iterator()
					while (memberIterator.hasNext()) {
						String userId = memberIterator.next()
						logVerbose("Found user [$userId] to use for group import.")
						try {
							// Attempt to import the user.
							actionsRunner.runAction([
								action: UcdImportLdapUsers.getSimpleName(),
								actionInfo: false,
								authenticationRealm: authenticationRealm,
								authorizationRealm: authorizationRealm,
								connectionPassword: connectionPassword,
								users: [ userId ]
							])

							// User import worked so quit.
							UcdUser ucdUser = actionsRunner.runAction([
								action: UcdGetUser.getSimpleName(),
								actionInfo: false,
								actionVerbose: false,
								user: userId,
								failIfNotFound: false
							])
					
							if (ucdUser) {
								break
							}
						} catch(Exception e) {
							// User import failed so try another one.
							logVerbose(e.getMessage())
						}
					}
					
					// Get the newly added group to validate it was added.
					ucdGroup = actionsRunner.runAction([
						action: UcdGetGroup.getSimpleName(),
						actionInfo: false,
						actionVerbose: false,
						group: groupName,
						failIfNotFound: true
					])

					// Synchronize the group's members.
					actionsRunner.runAction([
						action: UcdSyncLdapGroupMembers.getSimpleName(),
						actionInfo: false,
						authenticationRealm: authenticationRealm,
						authorizationRealm: authorizationRealm,
						connectionPassword: connectionPassword,
						group: groupName,
						maxSyncUsers: maxSyncUsers
					])
				} else {
					throw new UcAdfInvalidValueException("Unable to automatically add group [$groupName]. This could be because the group has only owners and no members.")
				}
			} else {
				throw new UcAdfInvalidValueException("LDAP group [$groupName] not found.")
			}
		}
	}
}
