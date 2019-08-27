/**
 * This action gets a component process request's properties.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetComponentProcessRequestProperties extends UcAdfAction {
	// Action properties.
	/** The component process request ID. */
	String requestId

	/**
	 * Runs the action.	
	 * @return The list of properties.
	 */
	@Override
	public List<UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdProperty> ucdProperties
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentProcessRequest/{requestId}/properties")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			Map responseMap = response.readEntity(new GenericType<Map<String, List<UcdProperty>>>(){})
			ucdProperties = responseMap.get("properties")
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdProperties
	}	
}
