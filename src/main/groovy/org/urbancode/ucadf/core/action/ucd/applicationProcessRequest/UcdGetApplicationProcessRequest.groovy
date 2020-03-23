/**
 * This action gets an application process request.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdGetApplicationProcessRequest extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String requestId
	
	/** The flag that indicates fail if the application process request is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The application process request object.
	 */
	@Override
	public UcdApplicationProcessRequest run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting application process request [$requestId].")

		UcdApplicationProcessRequest ucdApplicationProcessRequest
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcessRequest/{requestId}")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationProcessRequest = response.readEntity(UcdApplicationProcessRequest.class)
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() == 400 || response.status == 404) {
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}

		return ucdApplicationProcessRequest
	}
}
