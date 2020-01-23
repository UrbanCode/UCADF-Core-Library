/**
 * This action moves a resource from one path to another.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import groovy.json.JsonBuilder

class UcdRenameResource extends UcAdfAction {
	/** The resource path. */
	String resource
	
	/** The new resource name. */
	String name
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Rename resource [$resource] to [$name].")

		String resourceId = resource
		if (!UcdObject.isUUID(resource)) {
			UcdResource ucdToResource = actionsRunner.runAction([
				action: UcdGetResource.getSimpleName(),
				actionInfo: false,
				resource: resource,
				failIfNotFound: true
			])
			resourceId = ucdToResource.getId()
		}
		
		Map requestMap = [
			name: name
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/update")
			.queryParam("resource", resourceId)
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Resource [$resource] renamed to [$name].")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}
}
