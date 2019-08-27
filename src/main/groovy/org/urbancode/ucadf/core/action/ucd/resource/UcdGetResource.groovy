/**
 * This action gets a resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdGetResource extends UcAdfAction {
	/** The resource path or ID. */
	String resource
	
	/** The flag that indicates fail if the resource is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The resource object.
	 */
	@Override
	public UcdResource run() {
		// Validate the action properties.
		validatePropsExist()

		UcdResource ucdResource
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/info")
			.queryParam("resource", resource)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdResource = response.readEntity(UcdResource)
		} else if (response.getStatus() == 404 || response.getStatus() == 403) {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			if (failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			} else {
				logInfo("Resource [$resource] not found.")
			}
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdResource
	}
}
