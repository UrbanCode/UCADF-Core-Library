/**
 * This action adds a UCD server to the UCD cluster list on another UCD server.
 */
package org.urbancode.ucadf.core.action.ucd.cluster

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import groovy.json.JsonBuilder

// TODO: This needs to be tested.
class UcdAddServerToCluster extends UcAdfAction {
	// Action properites.
	/** The name used to identify the server in the list. */
	String name
	
	/** The server host. */
	String host
	
	/** The server port. */
	Integer port = 7918
	
	/** If true then active. Default is true. */
	Boolean active = true
	
	/** The flag that indicates fail if the server already exists in the cluster. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Save a network relay.
		logInfo("Adding server [$name] host [$host] port [$port] active [$active] to cluster.")
		
		Map requestMap = [
			name: name,
			host: host,
			port: port,
			active: active
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/network/networkRelay")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() != 200) {	
			logInfo("Server [$name] host [$host] port [$port] added to cluster.")
        } else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
        }
	}
}
