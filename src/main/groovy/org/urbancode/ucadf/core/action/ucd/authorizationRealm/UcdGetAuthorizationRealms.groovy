/**
 * This action gets a list of authorization realms.
 */
package org.urbancode.ucadf.core.action.ucd.authorizationRealm

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authorizationRealm.UcdAuthorizationRealm
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdGetAuthorizationRealms extends UcAdfAction {
	/**
	 * Runs the action.	
	 * @return The list of authorization realm objects.
	 */
	@Override
	public List<UcdAuthorizationRealm> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdAuthorizationRealm> ucdAuthorizationRealms = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/authorizationRealm")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdAuthorizationRealms = response.readEntity(new GenericType<List<UcdAuthorizationRealm>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
				
		return ucdAuthorizationRealms
	}
}
