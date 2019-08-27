/**
 * This action deletes an environment.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdDeleteEnvironment extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

	/** The flag that indicates to delete the attached resources. Default is false. */	
	Boolean deleteAttachedResources = false
	
	/** The flag that indicates fail if the environment is not found. Default is false. */
	Boolean failIfNotFound = false

	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/**
	 * Runs the action.	
	 * @return True if the environment was delted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		logInfo("Deleting application [$application] environment [$environment].")

		// Get the environment information in order to get the environment ID.		
		UcdEnvironment ucdEnvironment = actionsRunner.runAction([
			action: UcdGetEnvironment.getSimpleName(),
			application: application,
			environment: environment,
			failIfNotFound: failIfNotFound
		])

		if (ucdEnvironment) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/environment/{envId}")
				.resolveTemplate("envId", ucdEnvironment.getId())
				.queryParam("deleteAttachedResources", deleteAttachedResources)
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logInfo("Application [$application] environment [$environment] deleted.")
				deleted = true
			} else if (response.getStatus() == 404) {
				logInfo(response.readEntity(String.class))
				if (failIfNotFound) {
					throw new UcdInvalidValueException("Application [$application] environment [$environment] not found to delete.")
				}
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
		
		return deleted
	}
}
