/**
 * This action updates the system configuration.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

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
		Map existingConfigMap = actionsRunner.runAction([
			action: UcdGetSystemConfiguration.getSimpleName(),
			returnAs: UcdGetSystemConfiguration.ReturnAsEnum.MAP
		])
		
		// Can't use this because it comes back masked.
		existingConfigMap.remove('deployMailPassword')

		// Replace the affected properties.		
		configMap.each { key, value ->
			println "Replacing $key [${existingConfigMap.get(key)}] with [$value]."
			existingConfigMap.put(key, configMap.get(key))
		}
		
		logVerbose("Updating the system configuration.")

		JsonBuilder jsonBuilder = new JsonBuilder(existingConfigMap)
		println jsonBuilder

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/system/configuration")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 204) {
			logVerbose("System configuration updated.")
		} else {
			throw new UcAdfInvalidValueException(response)
		}
	}
}
