/**
 * This action deletes an authorization realm.
 */
package org.urbancode.ucadf.core.action.ucd.authorizationRealm

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealm
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdDeleteAuthorizationRealm extends UcAdfAction {
	// Action properties.
	/** The authorization realm name or ID. */
	String realm
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/** The flag that indicates fail if the authorization realm is not found. Default is true. */
	Boolean failIfNotFound = true
	
	// Private properties.
	private Boolean deleted = false
	
	/**
	 * Runs the action.	
	 * @return True if the authorization realm was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
		        
		if (!commit) {
			logInfo("Would delete realm [$realm].")
		} else {
			logInfo("Deleting realm [$realm].")
	
			if (UcdObject.isUUID(realm)) {
				deleteAuthorizationRealm(realm)
			} else {
				UcdAuthorizationRealm ucdAuthorizationRealm = actionsRunner.runAction([
					action: UcdGetAuthorizationRealm.getSimpleName(),
					realm: realm,
					failIfNotFound: failIfNotFound
				])
				
				if (ucdAuthorizationRealm) {
					deleteAuthorizationRealm(ucdAuthorizationRealm.getId())
				}
			}
		}
		
		return deleted
    }

	// Delete the authorization realm.	
	public deleteAuthorizationRealm(final String realmId) {
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/authorizationRealm/{realmId}")
			.resolveTemplate("realmId", realmId)
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() == 200 || response.getStatus() == 204) {
			logInfo("Application [$realmId] deleted.")
			deleted = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
	}
}
