/**
 * This action gets a component proccess request.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

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

		logVerbose("Getting component process request [$requestId].")

		UcdComponentProcessRequest ucdComponentProcessRequest
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/workflow/componentProcessRequest/{requestId}")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentProcessRequest = response.readEntity(UcdComponentProcessRequest.class)
			
			// This response can return a 200 but still be not found.
			if (!ucdComponentProcessRequest.getId()) {
				ucdComponentProcessRequest = null
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException("Component process request [$requestId] not found.")
				}
			}
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() == 400 || response.getStatus() == 404) {
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}

		return ucdComponentProcessRequest
	}
}
