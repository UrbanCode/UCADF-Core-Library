/**
 * This action gets an authorization realm.
 */
package org.urbancode.ucadf.core.action.ucd.authorizationRealm

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealm
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetAuthorizationRealm extends UcAdfAction {
	// Action properties.
	/** The realm name or ID. */
	String realm
	
	/** The flag that indicates fail if the authentication realm is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The authorization realm object.
	 */
	@Override
	public UcdAuthorizationRealm run() {
		// Validate the action properties.
		validatePropsExist()
		
		UcdAuthorizationRealm ucdAuthorizationRealm
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/authorizationRealm/{realm}")
			.resolveTemplate("realm", realm)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdAuthorizationRealm = response.readEntity(UcdAuthorizationRealm)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdAuthorizationRealm
	}
}
