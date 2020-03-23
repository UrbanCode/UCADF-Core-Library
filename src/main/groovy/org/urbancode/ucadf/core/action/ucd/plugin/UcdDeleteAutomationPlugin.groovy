/**
 * This action deletes an plugin.
 */
package org.urbancode.ucadf.core.action.ucd.plugin

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.plugin.UcdAutomationPlugin

class UcdDeleteAutomationPlugin extends UcAdfAction {
	// Action properties.
	/** The plugin name or ID. */
	String plugin
	
	/** The flag that indicates to delete all versions. Default is false. */
	Boolean deleteAllVersions = false
	
	/** The flag that indicates fail if the plugin is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true

	/**
	 * Runs the action.	
	 * @return True if the plugin was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean deleted = false
		
		if (!commit) {
			logVerbose("Would delete plugin [$plugin] deleteAllVersions=[$deleteAllVersions].")
		} else {
			UcdAutomationPlugin ucdAutomationPlugin = actionsRunner.runAction([
				action: UcdGetAutomationPlugin.getSimpleName(),
				actionInfo: false,
				plugin: plugin,
				failIfNotFound: failIfNotFound
			])

			while (ucdAutomationPlugin) {
				logVerbose("Delete plugin [$plugin] version [${ucdAutomationPlugin.getVersion()}].")
	
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/plugin/automationPlugin/{plugin}")
					.resolveTemplate("plugin", plugin)
				logDebug("target=$target")
				
				// Logic to handle concurrency issue that might exist in some UCD versions.
				final Integer MAXATTEMPTS = 5
				for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
					Response response = target.request(MediaType.APPLICATION_JSON).delete()
					
					if (response.status == 204) {
						logVerbose("Plugin [$plugin] deleted.")
						deleted = true
						break
					} else if (response.status == 404) {
						String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
						logVerbose(errMsg)
						if (failIfNotFound) {
							throw new UcAdfInvalidValueException(errMsg)
						}
						break
					} else {
						String responseStr = response.readEntity(String.class)
						logVerbose(responseStr)
						if (responseStr ==~ /.*bulk manipulation query.*/ && iAttempt < MAXATTEMPTS) {
							logVerbose("Attempt $iAttempt failed. Waiting to try again.")
							Thread.sleep(2000)
						} else {
							throw new UcAdfInvalidValueException(response)
						}
					}
				}

				// If deleting all versions then get the next one.
				if (deleteAllVersions) {
					ucdAutomationPlugin = actionsRunner.runAction([
						action: UcdGetAutomationPlugin.getSimpleName(),
						actionInfo: false,
						plugin: plugin,
						failIfNotFound: false
					])
				} else {
					ucdAutomationPlugin = null
				}
			}
		}
		
		return deleted
	}
}
