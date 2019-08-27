/**
 * This action gets a list of security types.
 */
package org.urbancode.ucadf.core.action.ucd.security

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityType

class UcdGetSecurityTypes extends UcAdfAction {
	// Static so the information is only loaded once.
	private static List<UcdSecurityType> ucdSecurityTypes
		
	/** 
	 * Runs the action 
	 * @return Returns a list of security type objects.
	 */
	@Override
	public List<UcdSecurityType> run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting security types.")

		// Only needs to be loaded once.
		if (!ucdSecurityTypes) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/resourceType")
			logDebug("target=$target")
	
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdSecurityTypes = response.readEntity(new GenericType<List<UcdSecurityType>>(){})
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
		
		return ucdSecurityTypes
	}
}
