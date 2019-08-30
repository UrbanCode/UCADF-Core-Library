/**
 * This action gets a component proccess request.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetComponentProcessRequest extends UcAdfAction {
	// Action properties.
	/** The component process request ID. */
	String requestId
	
	/** The flag that indicates fail if the component process request is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The component process request object.
	 */
	@Override
	public UcdComponentProcessRequest run() {
		// Validate the action properties.
		validatePropsExist()

		logInfo("Getting component process request [$requestId].")

		UcdComponentProcessRequest ucdComponentProcessRequest
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/workflow/componentProcessRequest/{requestId}")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentProcessRequest = response.readEntity(UcdComponentProcessRequest.class)
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
		
		return ucdComponentProcessRequest
	}
}
