/**
 * This action gets an application.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdGetApplication extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The flag that indicates fail if the application is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.
	 * @return The application object.
	 */
	public UcdApplication run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting application [$application].")
	
		// Get information about a application.
		UcdApplication ucdApplication
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/application/info")
			.queryParam("application", application)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplication = response.readEntity(UcdApplication.class)
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
        	if (response.getStatus() == 404 || response.getStatus() == 403) {
				// 403 added for UCD 16.2.
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException(errMsg)
				}
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return ucdApplication
	}
}
