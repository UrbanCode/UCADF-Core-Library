/**
 * This action creates a component resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import groovy.json.JsonBuilder

class UcdCreateComponentResource extends UcAdfAction {
	// Action properties.
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource path or ID. */
	String resource
	
	/** The component name or ID. */
	String component
	
	/** (Optional) The description. */
	String description = ""

	/** The flag that indicates fail if the component resource already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the resource was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean created = false
		
		String name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}
		
		logVerbose("Creating component resource [$parent/$name].")

		// Look up the role ID and use it instead of the name because of an UrbanCode defect that
		// sometimes incorrectly corrects the resource with the wrong role ID.
		UcdComponent ucdComponent = actionsRunner.runAction([
			action: UcdGetComponent.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			component: component
		])
		
		if (!ucdComponent) {
			throw new UcAdfInvalidValueException("Component [$component] not found.")
		}

		// Construct the request map.
		Map<String, String> requestMap = [
			name: name,
			parent: parent,
			description: description,
			role: ucdComponent.getResourceRole().get("id")
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=${jsonBuilder.toString()}")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/create")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			if (response.getStatus() == 400 && errMsg.matches(/.*already exists.*/)) {
				if (failIfExists) {
					throw new UcAdfInvalidValueException(errMsg)
				} else {
					logVerbose("Resource [$parent/$name] already exists.")
				}
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
