/**
 * This action gets the system configuration.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.system.UcdSystemConfiguration

class UcdGetSystemConfiguration extends UcAdfAction {
	/**
	 * Runs the action.	
	 */
	@Override
	public UcdSystemConfiguration run() {
		// Validate the action properties.
		validatePropsExist()
		
		UcdSystemConfiguration ucdSystemConfiguration
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/systemConfiguration")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdSystemConfiguration = response.readEntity(UcdSystemConfiguration.class)
		} else {
			throw new UcdInvalidValueException(response)
		}

		// Weird problem where the value it returns is multiplied by 1000 so we divide it to get it back to the right value.
		if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_61)) {
			ucdSystemConfiguration.setRepoAutoIntegrationPeriod(
				ucdSystemConfiguration.getRepoAutoIntegrationPeriod() / 1000
			)
		}
		
		return ucdSystemConfiguration
	}
}
