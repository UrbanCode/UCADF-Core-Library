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
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdSecureString
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

// Add LDAP groups.
// May provide authorizationRealm, authenticationRealm, and bindPw or ldapManager (for efficiency if called many times).
class UcdAddLdapGroups extends UcAdfAction {
	// Action properties.
	/** The authentication realm name or ID. */
	String authenticationRealm
	
	/** The authorization realm name or ID. */
	String authorizationRealm
	
	/** The bind password. */
	UcdSecureString bindPw = new UcdSecureString()
	
	/** The list of group names or IDs. */
	List<String> groups
	
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
			bindPw: bindPw.toString()
		])

		// Add the LDAP groups.
        addLdapGroups(groups*.trim())
	}	
	
	// Adds an LDAP group by finding a user of the group to import.
	public addLdapGroups(final List<String> groupNames) {
		logInfo("Adding LDAP groups to authentication realm [${authenticationRealm}].")
	
		for (groupName in groupNames) {
			UcdGroup ucdGroup = actionsRunner.runAction([
				action: UcdGetGroup.getSimpleName(),
				group: groupName,
				failIfNotFound: false
			])
			
			if (ucdGroup) {
				logInfo("Group [$groupName] already exists.")
				continue
			}
			
			logInfo("Attempting to automatically add group [$groupName].")
			List<LdapGroupResult> ldapGroupResults = ldapManager.getGroups(groupName, 1)
			if (ldapGroupResults && ldapGroupResults.size() > 0) {
				logInfo("Found group [$groupName] in LDAP. Looking for a user ID to use to import group.")
				List<LdapGroupUsersResult> ldapGroupUsersResults = ldapManager.getGroupUsers(groupName, 1)
				if (ldapGroupUsersResults && ldapGroupUsersResults.size() > 0 && ldapGroupUsersResults[0].getMembers().size() > 0) {
					logInfo("Group [$groupName] has ${ldapGroupUsersResults[0].getMembers().size()} members.")
					
					Iterator memberIterator = ldapGroupUsersResults[0].getMembers().iterator()
					while (memberIterator.hasNext()) {
						String userId = memberIterator.next()
						logInfo("Found user [$userId] to use for group import.")
						try {
							// Attempt to import the user.
							actionsRunner.runAction([
								action: UcdImportLdapUsers.getSimpleName(),
								users: [ userId ]
							])

							// User import worked so quit.
							UcdUser ucdUser = actionsRunner.runAction([
								action: UcdGetUser.getSimpleName(),
								user: userId,
								failIfNotFound: false
							])
					
							if (ucdUser) {
								break
							}
						} catch(Exception e) {
							// User import failed so try another one.
							logInfo(e.getMessage())
						}
					}
					
					// Get the newly added group to validate it was added.
					ucdGroup = actionsRunner.runAction([
						action: UcdGetGroup.getSimpleName(),
						team: groupName,
						failIfNotFound: true
					])

					// Synchronize the group's members.
					actionsRunner.runAction([
						action: UcdSyncLdapGroupMembers.getSimpleName()
					])
				} else {
					throw new UcdInvalidValueException("Unable to automatically add group [$groupName]. This could be because the group has only owners and no members.")
				}
			}
		}
	}
}
