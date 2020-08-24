/**
 * This action creates desired inventory version entries for an environment.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.action.ucd.version.UcdGetVersion
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import groovy.json.JsonBuilder

class UcdCreateDesiredInventoryVersions extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/** The request ID. */
	String requestId

	/** The list of version maps, e.g. [ { "comp1" : "version1"}, {"comp2" : "version2" } ] */
	List<Map<String, String>> versions = []

	/** (Optional) The inventory status. Default is Active. */
	String status = "Active"
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		logVerbose("Create desired inventory versions for application [$application] environment [$environment].")

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

		List<Map<String, String>> entries = []

		for (versionMap in versions) {
			versionMap.each { component, version ->
				println "Adding component [$component] version [$version] to list."
				
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
				
				// If a version ID was provided then use it. Otherwise get the version information to get the ID.
				String versionId = version
				if (!UcdObject.isUUID(version)) {
					UcdVersion ucdVersion = actionsRunner.runAction([
						action: UcdGetVersion.getSimpleName(),
						actionInfo: false,
						actionVerbose: false,
						component: component,
						version: version,
						failIfNotFound: true
					])
					
					versionId = ucdVersion.getId()
				}
				
				entries.add([
					environmentId: environmentId,
					componentId: componentId,
					versionId: versionId,
					status: status
				])
			}
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/inventory/desiredInventory/entries")
		logDebug("target=$target")

		Map requestMap = [
			deploymentRequest: requestId,
			entries: entries
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 204) {
			logVerbose("Inventory entries created.")
		} else {
			logError(response.readEntity(String.class))
			throw new UcAdfInvalidValueException("Status: ${response.getStatus()} Unable to create inventory entries. $target")
		}
	}
}
