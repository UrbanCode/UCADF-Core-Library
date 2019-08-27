/**
 * This action gets an authentication realm.
 */
package org.urbancode.ucadf.core.action.ucd.authenticationRealm

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.agentRelay.UcdAgentRelay
import org.urbancode.ucadf.core.model.ucd.authenticationRealm.UcdAuthenticationRealm
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetAuthenticationRealm extends UcAdfAction {
	// Action properties.
	/** The authentication realm name or ID. */
	String realm
	
	/** The flag that indicates fail if the authentication realm is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The authentication realm object.
	 */
	@Override
	public UcdAuthenticationRealm run() {
		// Validate the action properties.
		validatePropsExist()
		
		UcdAuthenticationRealm ucdAuthenticationRealm
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/authenticationRealm/{realm}")
			.resolveTemplate("realm", realm)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			List<UcdAuthenticationRealm> ucdAuthenticationRealms = response.readEntity(new GenericType<List<UcdAuthenticationRealm>>(){})
			ucdAuthenticationRealm = ucdAuthenticationRealms[0]
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdAuthenticationRealm
	}
}
