/**
 * This action creates a snapshot.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import groovy.json.JsonBuilder

class UcdCreateSnapshot extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name. */
	String name
	
	/** (Optional) The description. */
	String description = ""
	
	/** The list of version maps. */
	List<Map<String, String>> versions = []
	
	/** The flag that indicates fail if the snapshot already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the snapshot was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean created = false
		
		logInfo("Creating application [$application] snapshot [$name].")
		
		Map requestMap = [
			name: name, 
			application: application, 
			description: description, 
			versions: versions
		]
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/snapshot/createSnapshot")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.WILDCARD).accept(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logInfo("UrbanCode [$application] application snapshot [$name] created.")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
