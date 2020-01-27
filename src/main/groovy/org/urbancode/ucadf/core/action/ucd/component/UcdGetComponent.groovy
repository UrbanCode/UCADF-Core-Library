/**
 * This action gets a component.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetComponent extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The flag that indicates fail if the component is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The component object.
	 */
	@Override
	public UcdComponent run() {
		// Validate the action properties.
		validatePropsExist()

		UcdComponent ucdComponent
		
		logDebug("Getting component [$component].")
	
		// Get information about a component.
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/info")
			.queryParam("component", component)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponent = response.readEntity(UcdComponent.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
        	if (response.getStatus() == 404 || response.getStatus() == 403) {
				if (failIfNotFound) {
					throw new UcdInvalidValueException(errMsg)
				}
			} else {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		return ucdComponent
	}
}
