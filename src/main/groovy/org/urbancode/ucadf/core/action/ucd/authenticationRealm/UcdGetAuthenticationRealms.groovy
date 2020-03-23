/**
 * This action gets a list of authentication realms.
 */
package org.urbancode.ucadf.core.action.ucd.authenticationRealm

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authenticationRealm.UcdAuthenticationRealm
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdGetAuthenticationRealms extends UcAdfAction {
	/**
	 * Runs the action.	
	 * @return The list of authentication realm objects.
	 */
	@Override
	public List<UcdAuthenticationRealm> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdAuthenticationRealm> ucdAuthenticationRealms = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/authenticationRealm")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdAuthenticationRealms = response.readEntity(new GenericType<List<UcdAuthenticationRealm>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
				
		return ucdAuthenticationRealms
	}
}
