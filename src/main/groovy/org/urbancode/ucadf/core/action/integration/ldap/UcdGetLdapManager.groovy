/**
 * This action gets an LDAP manager object for the specified realms. The LDAP manager is saved by realm as an actions runner property.
 */
package org.urbancode.ucadf.core.action.integration.ldap

import org.urbancode.ucadf.core.action.ucd.authenticationRealm.UcdGetAuthenticationRealm
import org.urbancode.ucadf.core.action.ucd.authorizationRealm.UcdGetAuthorizationRealm
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.integration.ldap.LdapManager
import org.urbancode.ucadf.core.model.ucd.authenticationRealm.UcdAuthenticationRealm
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealm
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString

class UcdGetLdapManager extends UcAdfAction {
	// Action properties.
	/** The LDAP authentication realm. */
	String authenticationRealm
	
	/** The LDAP authorization realm. */
	String authorizationRealm
	
	/** (Optional) The LDAP connection password. */
	UcAdfSecureString connectionPassword = new UcAdfSecureString()
	
	private ldapManagerPropertyName
	
	/**
	 * Runs the action.	
	 * @return The LDAP manager object.
	 */
	@Override
	public LdapManager run() {
		// Validate the action properties.
		validatePropsExist()

		// The property where the LDAP manager will be stored.
		ldapManagerPropertyName = "UcdLdapManager-${authenticationRealm}-${authorizationRealm}"
		
		LdapManager ldapManager
		
		// If the LDAP manager has already been initialized then use it.
		if (actionsRunner.propertyValueExists(ldapManagerPropertyName)) {
			ldapManager = actionsRunner.getPropertyValue(
				ldapManagerPropertyName
			)
		} else {
			// Get the authentication realm object.
			UcdAuthenticationRealm ucdAuthenticationRealm = actionsRunner.runAction([
				action: UcdGetAuthenticationRealm.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				realm: authenticationRealm
			])
	
			// Get the authorization realm object.
			UcdAuthorizationRealm ucdAuthorizationRealm = actionsRunner.runAction([
				action: UcdGetAuthorizationRealm.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				realm: authorizationRealm
			])
		
			// Initialize an LDAP manager.	
			ldapManager = new LdapManager(
				ucdAuthenticationRealm.getProperties().getUrl(),
				ucdAuthenticationRealm.getProperties().getConnectionName(),
				connectionPassword.toString(),
				ucdAuthenticationRealm.getProperties().getUserBase(),
				ucdAuthorizationRealm.getProperties().getGroupBase()
			)

			// Save the LDAP manager object as a runner property value.
			logVerbose("LDAP manager property is [$ldapManagerPropertyName].")
			actionsRunner.setPropertyValue(
				ldapManagerPropertyName,
				ldapManager
			)
		}
		
		return ldapManager
	}
}
