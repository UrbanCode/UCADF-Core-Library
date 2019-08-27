/**
 * This action gets an application process request's properties.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetApplicationProcessRequestProperties extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String requestId

	/**
	 * Runs the action.	
	 * @return The map of application process request properties having the property name as the key and the value a property object.
	 */
	@Override
	public Map<String, UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		Map<String, UcdProperty> propertiesMap = [:]
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcessRequest/{requestId}/properties")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			Map responseMap = response.readEntity(new GenericType<Map<String, List<UcdProperty>>>(){})
			List<UcdProperty> ucdProperties = responseMap.get("properties")
			for (ucdProperty in ucdProperties) {
				propertiesMap.put(ucdProperty.getName(), ucdProperty)
			}
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return propertiesMap
	}	
}
