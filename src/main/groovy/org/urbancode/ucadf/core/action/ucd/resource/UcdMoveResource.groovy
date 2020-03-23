/**
 * This action moves a resource from one path to another.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import groovy.json.JsonBuilder

class UcdMoveResource extends UcAdfAction {
	/** The resource from path. */
	String resourceFrom
	
	/** The resource to path. */
	String resourceTo
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Move resource [$resourceFrom] to [resourceTo].")

		// Get the resource information.
		String resourceFromId = resourceFrom
		if (!UcdObject.isUUID(resourceFrom)) {
			UcdResource ucdFromResource = actionsRunner.runAction([
				action: UcdGetResource.getSimpleName(),
				actionInfo: false,
				resource: resourceFrom,
				failIfNotFound: true
			])
			resourceFromId = ucdFromResource.getId()
		}
		
		String resourceToId = resourceTo
		if (!UcdObject.isUUID(resourceTo)) {
			UcdResource ucdToResource = actionsRunner.runAction([
				action: UcdGetResource.getSimpleName(),
				actionInfo: false,
				resource: resourceTo,
				failIfNotFound: true
			])
			resourceToId = ucdToResource.getId()
		}
		
		Map requestMap = [
			sources: [
				[ id: resourceFromId ]
			]
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/resource/resource/moveTo/{resourceToId}")
			.resolveTemplate("resourceToId", resourceToId)
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("resource [$resourceFrom] moved to [$resourceTo].")
		} else {
			throw new UcAdfInvalidValueException(response)
		}
	}
}
