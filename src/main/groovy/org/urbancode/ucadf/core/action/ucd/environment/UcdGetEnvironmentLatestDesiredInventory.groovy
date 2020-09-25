/**
 * This action gets an environment's latest desired inventory.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironmentLatestDesiredInventory
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetEnvironmentLatestDesiredInventory extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

	/** The flag indicating to group versions. */
	Boolean groupVersions = false
		
	/**
	 * Runs the action.	
	 * @return The environment object.
	 */
	@Override
	public List<UcdEnvironmentLatestDesiredInventory> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdEnvironmentLatestDesiredInventory> latestDesiredInventoryList
		
		logVerbose("Getting application [$application] environment [$environment] latest desired inventory.")

		// If an environment ID was provided then use it. Otherwise get the environment information to get the ID.
		String environmentId = environment
		if (!UcdObject.isUUID(environment)) {
			UcdEnvironment ucdEnvironment = actionsRunner.runAction([
				action: UcdGetEnvironment.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				application: application,
				environment: environment,
				failIfNotFound: true
			])
			
			environmentId = ucdEnvironment.getId()
		}

		WebTarget target 
		if (groupVersions) {
			target = ucdSession.getUcdWebTarget().path("/cli/environment/{environmentId}/latestDesiredInventory/true")
				.resolveTemplate("environmentId", environmentId)
		} else {
			target = ucdSession.getUcdWebTarget().path("/cli/environment/{environmentId}/latestDesiredInventory")
				.resolveTemplate("environmentId", environmentId)
		}
		logDebug("target=$target")

		Response response = target.request().get()

		if (response.getStatus() == 200) {
			latestDesiredInventoryList = response.readEntity(new GenericType<List<UcdEnvironmentLatestDesiredInventory>>(){})
			
			// For some reason the version's component value is not set on the return so iterate the list and set it.
			for (inventoryItem in latestDesiredInventoryList) {
				inventoryItem.getVersion().setComponent(inventoryItem.getComponent())
			}
		} else {
			throw new UcAdfInvalidValueException(response)
		}
		
		return latestDesiredInventoryList
	}
}
