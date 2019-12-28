/**
 * This action adds resource inventory.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.action.ucd.version.UcdGetVersion
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import groovy.json.JsonBuilder

class UcdAddResourceInventory extends UcAdfAction {
	// Action properties.
	/** The associated application process request ID. */
	String requestId
	
	/** The resource path or ID. */
	String resource
	
	/** The component name or ID. */
	String component

	/** The version name or ID. */	
	String version
	
	/** The inventory status name or ID. */
	String status
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		logInfo("Creating inventory entry on resource [$resource] for component [$component] version [$version] for application process request [$requestId].")

		UcdResource ucdResource = actionsRunner.runAction([
			action: UcdGetResource.getSimpleName(),
			actionInfo: false,
			resource: resource
		])
		
		UcdComponent ucdComponent = actionsRunner.runAction([
			action: UcdGetComponent.getSimpleName(),
			actionInfo: false,
			component: component
		])

		UcdVersion ucdVersion = actionsRunner.runAction([
			action: UcdGetVersion.getSimpleName(),
			actionInfo: false,
			component: component,
			version: version
		])
		
		Map requestMapEntry = [
			resourceId: ucdResource.getId(),
			componentId: ucdComponent.getId(),
			versionId: ucdVersion.getId(),
			status: status
		]
		
		List entries = []
		entries.add(requestMapEntry)

		Map requestMap = [
			deploymentRequest: requestId,
			entries: entries
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/inventory/entries")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() != 204) {	
			throw new UcdInvalidValueException(response)
		}
	}
}
