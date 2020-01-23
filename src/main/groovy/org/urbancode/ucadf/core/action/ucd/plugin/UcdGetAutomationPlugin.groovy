/**
 * This action gets an automation plugin object.
 */
package org.urbancode.ucadf.core.action.ucd.plugin

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.plugin.UcdAutomationPlugin

class UcdGetAutomationPlugin extends UcAdfAction {
	/** The plugin name or ID. */
	String plugin
	
	/** The flag that indicates fail if the automation plugin is not found. Default is true. */
	Boolean failIfNotFound = true

	/**
	 * Runs the action.
	 * @return Returns the plugin object.
	 */
	public UcdAutomationPlugin run() {
		// Validate the action properties.
		validatePropsExist()

		UcdAutomationPlugin ucdReturnPlugin
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/plugin/automationPlugin/{plugin}")
			.resolveTemplate("plugin", plugin)
		logDebug("target=$target")
			
		// Get the automation plugin.			
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdReturnPlugin = target.request().get(UcdAutomationPlugin)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 404 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		return ucdReturnPlugin
	}	
}
