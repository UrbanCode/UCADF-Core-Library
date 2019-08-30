/**
 * This action gets an environment's inventory by resource.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironmentResourceInventory
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdGetEnvironmentInventoryByResource extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/**
	 * Runs the action.	
	 * @return The environment resource inventory object.
	 */
	@Override
	public List<UcdEnvironmentResourceInventory> run() {
		// Validate the action properties.
		validatePropsExist()
		
		List<UcdEnvironmentResourceInventory> ucdEnvironmentResourceInventory

		logInfo("Get environment inventory by resource for application [$application] environment [$environment].")

		// Get the environment information in order to get the environment ID.		
		UcdEnvironment ucdEnvironment = actionsRunner.runAction([
			action: UcdGetEnvironment.getSimpleName(),
			application: application,
			environment: environment,
			failIfNotFound: true
		])

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/inventory/environmentInventoryByResource/{environmentId}")
			.resolveTemplate("environmentId", ucdEnvironment.getId())
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdEnvironmentResourceInventory = response.readEntity(new GenericType<List<UcdEnvironmentResourceInventory>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdEnvironmentResourceInventory
	}
}
