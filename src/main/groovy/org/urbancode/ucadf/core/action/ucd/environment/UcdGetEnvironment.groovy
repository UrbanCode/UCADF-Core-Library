/**
 * This action gets an environment.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdGetEnvironment extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

	/** The flag that indicates to return more details. TODO: What are the details? */	
	Boolean withDetails = false
	
	/** The flag that indicates fail if the environment is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The environment object.
	 */
	@Override
	public UcdEnvironment run() {
		// Validate the action properties.
		validatePropsExist()

		UcdEnvironment ucdEnvironment
		
		logVerbose("Getting application [$application] environment [$environment].")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/info")
			.queryParam("application", application)
			.queryParam("environment", environment)
		logDebug("target=$target")
		
		Response response = target.request().get()

		if (response.getStatus() == 200) {
			ucdEnvironment = response.readEntity(UcdEnvironment)
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
		
		// If details are desired then do a different API call that requires the environment ID.
		if (ucdEnvironment && withDetails) {
			target = ucdSession.getUcdWebTarget().path("/rest/deploy/environment/{envId}")
				.resolveTemplate("envId", ucdEnvironment.getId())
			logDebug("target=$target")
			
			response = target.request().get()
			if (response.getStatus() == 200) {
				ucdEnvironment = response.readEntity(UcdEnvironment)
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
		}
		
		return ucdEnvironment
	}
}
