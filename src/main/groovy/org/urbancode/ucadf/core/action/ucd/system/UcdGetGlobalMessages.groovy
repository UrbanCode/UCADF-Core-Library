/**
 * This action deletes a system property.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.system.UcdGlobalMessage

class UcdGetGlobalMessages extends UcAdfAction {
	/**
	 * Runs the action.	
	 * @return The list of global messages.
	 */
	@Override
	public List<UcdGlobalMessage> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdGlobalMessage> globalMessages
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/state/globalMessages")
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			globalMessages = response.readEntity(new GenericType<List<UcdGlobalMessage>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		return globalMessages
	}
}
