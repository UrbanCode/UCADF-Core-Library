/**
 * This action gets a security type.
 */
package org.urbancode.ucadf.core.action.ucd.security

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityType
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityTypeEnum

class UcdGetSecurityType extends UcAdfAction {
	// Action properties.
	/** 
	 * The security type to get.
	 * May be either a {@link UcdSecurityTypeEnum} name or a ID.
	 */
	String type
	
	/** The flag that indicates fail if the subtype is not found. Default is true. */
	Boolean failIfNotFound = true

	/**
	 * Runs the action.
	 * @return Returns a security type object.
	 */
	@Override
	public UcdSecurityType run() {
		// Validate the action properties.
		validatePropsExist()

		UcdSecurityType ucdSecurityType

		WebTarget target = ucdSession.getUcdWebTarget().path("/security/resourceType/{type}")
			.resolveTemplate("type", type)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdSecurityType = response.readEntity(UcdSecurityType.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if ((response.getStatus() != 404) || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		return ucdSecurityType
	}
}
