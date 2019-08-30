/**
 * This action deletes a security subtype.
 */
package org.urbancode.ucadf.core.action.ucd.security

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdDeleteSecuritySubtype extends UcAdfAction {
	// Action properties.
	/** The security subtype name. */
	String subtype

	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
 	/** The flag that indicates fail if the specified subtype is not found. Default is false.*/
	Boolean failIfNotFound = false

	/** 
	 * Runs the action 
	 * @return Returns true if it was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
		
		if (commit) {
			logInfo("Deleting security subtype [$subtype].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/resourceRole/{subtype}")
				.resolveTemplate("subtype", subtype)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.WILDCARD).delete()
			if (response.getStatus() == 200) {
				logInfo("Security subtype [$subtype] deleted.")
				deleted = true
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logInfo(errMsg)
				// Some versions return 500.
				if ((response.getStatus() != 404 && response.getStatus() != 500) || failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		} else {
			logInfo("Would delete security subtype [$subtype].")
		}
		
		return deleted
	}
}
