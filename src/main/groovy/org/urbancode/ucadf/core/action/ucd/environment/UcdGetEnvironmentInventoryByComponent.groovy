/**
 * This action gets an environment's inventory by component.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironmentComponentInventory
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetEnvironmentInventoryByComponent extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/**
	 * Runs the action.	
	 * @return The list of environment component inventory objects.
	 */
	@Override
	public List<UcdEnvironmentComponentInventory> run() {
		// Validate the action properties.
		validatePropsExist()
		
		List<UcdEnvironmentComponentInventory> ucdInventory

		logVerbose("Get environment inventory by component for application [$application] environment [$environment].")

		// Get the environment information in order to get the environment ID.		
		UcdEnvironment ucdEnvironment = actionsRunner.runAction([
			action: UcdGetEnvironment.getSimpleName(),
			application: application,
			environment: environment,
			failIfNotFound: true
		])

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/inventory/environmentInventoryByComponent/{environmentId}")
			.resolveTemplate("environmentId", ucdEnvironment.getId())
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdInventory = response.readEntity(new GenericType<List<UcdEnvironmentComponentInventory>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdInventory
	}
}
