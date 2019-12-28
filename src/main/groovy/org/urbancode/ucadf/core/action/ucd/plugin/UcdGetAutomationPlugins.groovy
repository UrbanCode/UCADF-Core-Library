/**
 * This action gets a list of plugin objects.
 */
package org.urbancode.ucadf.core.action.ucd.plugin

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.plugin.UcdAutomationPlugin

class UcdGetAutomationPlugins extends UcAdfAction {
	/**
	 * Runs the action.
	 * @return Returns the list of automation plugin objects.
	 */
	public List<UcdAutomationPlugin> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdAutomationPlugin> ucdReturnPlugins = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/plugin/automationPlugin")

		// Get the list of automation plugins.			
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdReturnPlugins = target.request().get(new GenericType<List<UcdAutomationPlugin>>(){})
		} else {
			throw new UcdInvalidValueException(response)
		}

		return ucdReturnPlugins
	}	
}
