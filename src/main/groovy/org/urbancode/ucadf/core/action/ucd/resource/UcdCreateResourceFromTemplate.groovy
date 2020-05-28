/**
 * This action creates a resource from a resource template.
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

class UcdCreateResourceFromTemplate extends UcAdfAction {
	// Action properties.
	/** (Optional) The parent path or ID. */
	String parent

	/** The resource path or ID. */
	String resource
	
	/** The template name or ID> */	
	String template
	
	/** The flag that indicates fail if the resource already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		String name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}

		String resourcePath = "${parent}/${name}"
		
		logVerbose("Creating resource [$resourcePath] from template [$template.")

		// Make sure the parent resource exists.
		UcdResource ucdParentResource = actionsRunner.runAction([
			action: UcdGetResource.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			resource: parent,
			failIfNotFound: true
		])

		Map requestMap = [
			name: name,
			resourceTemplateId: template,
			targetResourceId: parent
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/resource/resource/addFromTemplate")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Resource [$name] added to [$parent] from template [$template].")
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && !failIfExists)) {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
	}
}
