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
	
	/**
	 * Runs the action.	
	 * @return True if the group resource was created.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		// If no parent path was provided then split the provided path to get the parent.
		String name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}

		logVerbose("Creating group resource [$parent/$name].")

		// Construct the request map.
		Map<String, String> requestMap = [
			name: name,
			parent: parent,
			description: description
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=${jsonBuilder.toString()}")

		WebTarget target 
		Response response
		
		target = ucdSession.getUcdWebTarget().path("/cli/resource/create")
		logDebug("target=$target")
		
		response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			created = true
			
			// Get the resource to verify it exists and to get the ID.
			target = ucdSession.getUcdWebTarget().path("/cli/resource/info")
				.queryParam("resource", resource)
			logDebug("target=$target")
		
			response = target.request().get()
			if (response.getStatus() != 200) {
				println response.getStatus()
			}
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
