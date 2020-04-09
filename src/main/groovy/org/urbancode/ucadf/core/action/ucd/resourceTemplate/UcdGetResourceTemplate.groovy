/**
 * This action gets an resource template.
 */
package org.urbancode.ucadf.core.action.ucd.resourceTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resourceTemplate.UcdResourceTemplate

class UcdGetResourceTemplate extends UcAdfAction {
	// Action properties.
	/** The resource template name or ID. */
	String resourceTemplate
	
	/** The flag that indicates fail if the resource template is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.
	 * @return The resource template object.
	 */
	public UcdResourceTemplate run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting resource template [$resourceTemplate].")

		UcdResourceTemplate ucdResourceTemplate
		
		// Get the resource template details.
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resourceTemplate")
			.queryParam("template", resourceTemplate)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdResourceTemplate = response.readEntity(UcdResourceTemplate.class)
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() == 404 || response.getStatus() == 403) {
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}

		return ucdResourceTemplate
	}
}
