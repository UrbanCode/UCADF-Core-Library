/**
 * This action gets the system configuration.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.system.UcdSystemConfiguration

class UcdGetSystemConfiguration extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a UcdSystemConfiguration object. */
		OBJECT,
		
		/** Return as a map. */
		MAP
	}
	
	/** The type of colleciton to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.OBJECT

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		Object returnSystemConfiguration
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/systemConfiguration")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			if (ReturnAsEnum.OBJECT.equals(returnAs)) {
				returnSystemConfiguration = response.readEntity(UcdSystemConfiguration.class)
			} else {
				returnSystemConfiguration = response.readEntity(Map.class)
			}
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		// Weird problem where the value it returns is multiplied by 1000 so we divide it to get it back to the right value.
		if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_61)) {
			returnSystemConfiguration['repoAutoIntegrationPeriod'] = (Long.valueOf(returnSystemConfiguration['repoAutoIntegrationPeriod']) / 1000).toString()
		}
		
		return returnSystemConfiguration
	}
}
