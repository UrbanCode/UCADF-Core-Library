/**
 * This action imports LDAP users.
 */
package org.urbancode.ucadf.core.action.ucd.user

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.integration.ldap.UcdGetLdapManager
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.integration.ldap.LdapManager
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdSecureString

class UcdImportLdapUsers extends UcAdfAction {
	// Action properties.
	/** The authentication realm name or ID. */
	String authenticationRealm
	
	/** The authorization realm name or ID. */
	String authorizationRealm
	
	/** The connection password. */
	UcdSecureString connectionPassword = new UcdSecureString()
	
	/** The list of user names. */
	List<String> users = []
	
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
		
		for (user in users) {
			String userName = user.trim()
			
			logInfo("Importing user [$userName] into authentication realm [$authenticationRealm].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/authenticationRealm/{realmName}/importUsers/{userName}")
				.resolveTemplate("realmName", authenticationRealm)
				.resolveTemplate("userName", userName)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.text(""))
			if (response.getStatus() == 200) {
				"User [$userName] imported."
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
