/**
 * This action gets a security subtype.
 */
package org.urbancode.ucadf.core.action.ucd.security

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype

class UcdGetSecuritySubtype extends UcAdfAction {
	// Action properties.
	/** The security subtype to get. */	
	String subtype
	
	/** The flag that indicates fail if the subtype is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.
	 * @return Returns the security subtype object.
	 */
	@Override
	public UcdSecuritySubtype run() {
		// Validate the action properties.
		validatePropsExist()

		UcdSecuritySubtype ucdSecuritySubtype

		// Don't attempt to get information for a blank subtype.
		if (subtype) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/resourceRole/{subtype}")
				.resolveTemplate("subtype", subtype)
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdSecuritySubtype = response.readEntity(UcdSecuritySubtype.class)
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if ((response.getStatus() != 404) || failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}
		
		return ucdSecuritySubtype
	}
}
