/**
 * This action deletes an authentication realm.
 */
package org.urbancode.ucadf.core.action.ucd.authenticationRealm

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authenticationRealm.UcdAuthenticationRealm
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdDeleteAuthenticationRealm extends UcAdfAction {
	// Action properties.
	/** The authentication realm name or ID. */
	String realm
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the authentication realm is not found. Default is false. */
	Boolean failIfNotFound = false
	
	// Private properties.
	private Boolean deleted = false
	
	/**
	 * Runs the action.	
	 * @return True if the authentication realm was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
		        
		if (!commit) {
			logVerbose("Would delete realm [$realm].")
		} else {
			logVerbose("Deleting realm [$realm].")
	
			if (UcdObject.isUUID(realm)) {
				deleteAuthenticationRealm(realm)
			} else {
				UcdAuthenticationRealm ucdAuthenticationRealm = actionsRunner.runAction([
					action: UcdGetAuthenticationRealm.getSimpleName(),
					actionInfo: false,
					realm: realm,
					failIfNotFound: failIfNotFound
				])
				
				if (ucdAuthenticationRealm) {
					deleteAuthenticationRealm(ucdAuthenticationRealm.getId())
				}
			}
		}
		
		return deleted
    }

	// Delete the authentication realm.	
	public deleteAuthenticationRealm(final String realmId) {
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/authenticationRealm/{realmId}")
			.resolveTemplate("realmId", realmId)
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() == 200 || response.getStatus() == 204) {
			logVerbose("Application [$realmId] deleted.")
			deleted = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
	}
}
