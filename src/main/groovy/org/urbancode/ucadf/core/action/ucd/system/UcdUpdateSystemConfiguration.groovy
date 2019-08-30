/**
 * This action updates the system configuration.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.system.UcdSystemConfiguration

import groovy.json.JsonBuilder

class UcdUpdateSystemConfiguration extends UcAdfAction {
	// Action properties.
	/** The configuration map. */
	Map<String, Object> configMap
    
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		// Get the UrbanCode system configuration.
		UcdSystemConfiguration ucdSystemConfiguration = actionsRunner.runAction([
			action: UcdGetSystemConfiguration.getSimpleName()
		])
		
		// Replace the affected properties.		
		configMap.each { key, value ->
			println "Replacing $key [${ucdSystemConfiguration.getProperty(key)}] with [$value]."
		}
		
		logInfo("Updating the system configuration.")

		Map requestMap = [:]
		configMap.each { k, v ->
			println "$k=$v"
			requestMap.put(k, v)
		}

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		println jsonBuilder

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/system/configuration")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 204) {
			logInfo("System configuration updated.")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}
}
