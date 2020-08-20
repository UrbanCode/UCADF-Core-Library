/**
 * This action creates an agent resource.
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

class UcdCreateAgentResource extends UcAdfAction {
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource path or ID. */
	String resource

	/** The agent name or ID. */	
	String agent
	
	/** (Optional) The description. */
	String description = ""

	/** The flag that indicates fail if the agent resource exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the agent resource was created.
	 */
	@Override
	public Object run() {
		validatePropsExist()

		Boolean created = false
				
		// If no parent path was provided then split the provided path to get the parent.
		String name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}

		logVerbose("Creating agent resource [$parent/$name].")

		// Construct the request map.
		Map<String, String> requestMap = [
			name: name,
			parent: parent,
			description: description,
			agent: agent
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
