/**
 * This action updates a resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import groovy.json.JsonBuilder

class UcdUpdateResource extends UcAdfAction {
	/** The resource path or ID. */
	String resource

	/** (Optional) The name. */	
	String name
	
	/** (Optional) THe description. */
	String description
	
	/** The flag that indicates inherit team. */
	Boolean inheritTeam
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistInclude(
			[
				'resource'
			]
		)
		
		// Get the resource information.
		UcdResource ucdResource = actionsRunner.runAction([
			action: UcdGetResource.getSimpleName(),
			actionInfo: false,
			resource: resource,
			failIfNotFound: true
		])

		Map requestMap = [
			name: name ?: ucdResource.getName(),
			dynamic: false,
			description: description ?: ucdResource.getDescription(),
			existingId: ucdResource.getId(),
            parentId: ucdResource.getParent().getId()
		]

		if (inheritTeam != null) {
			if (inheritTeam == true) {
				requestMap.put('inheritTeam', "true")
			} else {
				requestMap.put('inheritTeam', "false")
				requestMap.put('teamMappings', [])
			}
		}
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

        WebTarget target = ucdSession.getUcdWebTarget().path("/rest/resource/resource")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Resource [$resource] updated.")
		} else {
			throw new UcAdfInvalidValueException(response)
		}
	}
}
