/**
 * This action gets a list of system properties.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetSystemProperties extends UcAdfAction {
	/**
	 * Runs the action.	
	 * @return Returns a list of system property objects.
	 */
	@Override
	public List<UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdProperty> ucdSystemProperties

		logVerbose("Getting system properties.")
				
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/systemConfiguration/getProperties")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdSystemProperties = response.readEntity(new GenericType<List<UcdProperty>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdSystemProperties
	}
}
