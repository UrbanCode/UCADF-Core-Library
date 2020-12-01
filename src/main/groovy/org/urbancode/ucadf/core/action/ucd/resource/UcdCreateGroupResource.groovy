/**
 * This action creates a group resource.
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

class UcdCreateGroupResource extends UcAdfAction {
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource path or ID. */
	String resource

	/** (Optional) The description. */	
	String description = ""
	
	/** The flag that indicates fail if the group resource already exists. Default is true. */
	Boolean failIfExists = true

	// Private properties.
	private String name
	private Boolean created = false
	
	/**
	 * Runs the action.	
	 * @return True if the group resource was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		// If no parent path was provided then split the provided path to get the parent.
		name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}

		// Attempt to create the resource. If it encounters a unique result error then try to delete it and try to create it again.
		if (createResource(false)) {
			// Attempt to delete the resource.
			actionsRunner.runAction([
				action: UcdDeleteResource.getSimpleName(),
				actionInfo: true,
				actionVerbose: true,
				resource: resource
			])
			
			// Attempt to create the resource again.
			createResource(true)
		}

		// If the resource was created then attempt to get it to make sure there's no unique result problem.
		if (created) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/info")
				.queryParam("resource", "${parent}/${name}")
			logDebug("target=$target")
		
			Response response = target.request().get()
			
			// If the get encounters a unique result error then delete the resource and create it again.
			if (response.getStatus() == 400 && response.readEntity(String.class).matches(/.*query did not return a unique result.*/)) {
				// Attempt to delete the resource.
				actionsRunner.runAction([
					action: UcdDeleteResource.getSimpleName(),
					actionInfo: true,
					actionVerbose: true,
					resource: resource
				])
				
				// Attempt to create the resource again.
				createResource(true)
			}
		}
		
		return created
	}

	// Attempt to create the resource.	
	public Boolean createResource(final Boolean retry) {
		Boolean uniqueResultError = false
		
		logVerbose("Creating group resource [$parent/$name].")

		// Construct the request map.
		Map<String, String> requestMap = [
			name: name,
			parent: parent,
			description: description
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=${jsonBuilder.toString()}")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/create")
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON)
			.put(Entity.json(jsonBuilder.toString()))
			
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
			} else if (!retry && response.getStatus() == 400 && errMsg.matches(/.*query did not return a unique result.*/)) {
				uniqueResultError = true
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return uniqueResultError
	}
}
