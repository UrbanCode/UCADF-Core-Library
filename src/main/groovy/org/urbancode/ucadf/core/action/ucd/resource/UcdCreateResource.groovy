/**
 * This action creates a resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import groovy.json.JsonBuilder

class UcdCreateResource extends UcAdfAction {
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource name. */
	String name = ""

	/** The role. */	
	String role = ""
	
	/** The description. */
	String description = ""
	
	/** (Optional) The agent. */
	String agent = ""
	
	/** (Optional) The agent pool. */
	String agentPool = ""
	
	/** The flag that indicates fail if the resource already exists. Default is true. */
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
		
		// Construct the request map.
		Map<String, String> requestMap = [:]

		if (name) { requestMap.put("name", name) }
		if (parent) { requestMap.put("parent", parent) }
		if (role) { requestMap.put("role", role) }
		if (description) { requestMap.put("description", description) }
		if (agent) { requestMap.put("agent", agent) }
		if (agentPool) { requestMap.put("agentPool", agentPool) }
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=${jsonBuilder.toString()}")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/create")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logVerbose("Resource [$name] created.")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
