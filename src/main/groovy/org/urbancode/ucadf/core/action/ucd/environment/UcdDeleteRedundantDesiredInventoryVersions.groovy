/**
 * This action deletes an environment's redundant desired inventory.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdDeleteRedundantDesiredInventoryVersions extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/** The component name or ID. */
	String component
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		logVerbose("Delete all redundant desired inventory versions from application [$application] environment [$environment] for component [$component].")

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
		
		// If a component ID was provided then use it. Otherwise get the component information to get the ID.
		String componentId = component
		if (!UcdObject.isUUID(component)) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				failIfNotFound: true
			])
			
			componentId = ucdComponent.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/{environmentId}/redundantVersions/{componentId}")
			.resolveTemplate("environmentId", environmentId)
			.resolveTemplate("componentId", componentId)
			.queryParam("deleteRedundant", true)
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).delete()
		if (response.getStatus() == 200) {
			logVerbose("All redundant desired inventory versions deleted.")
		} else {
			logError(response.readEntity(String.class))
			throw new UcAdfInvalidValueException("Status: ${response.getStatus()} Unable to delete redundant inventory. $target")
		}
	}
}
