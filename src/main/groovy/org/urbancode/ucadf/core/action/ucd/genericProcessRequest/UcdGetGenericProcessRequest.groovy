/**
 * This action gets a generic process request.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcessRequest.UcdGenericProcessRequest

class UcdGetGenericProcessRequest extends UcAdfAction {
	// Action properties.
	String requestId
	
	/** The flag that indicates fail if the generic process request is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The generic process request object.
	 */
	@Override
	public UcdGenericProcessRequest run() {
		// Validate the action properties.
		validatePropsExist()

		UcdGenericProcessRequest ucdGenericProcessRequest
		
		logInfo("Getting generic process request [$requestId].")

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/request/{requestId}")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdGenericProcessRequest = response.readEntity(UcdGenericProcessRequest.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() == 400 || response.status == 404) {
				if (failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			} else {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdGenericProcessRequest
	}
}
