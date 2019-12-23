/**
 * This action gets an generic process request's properties.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

class UcdGetGenericProcessRequestProperties extends UcAdfAction {
	/** The type of collection to return. */
	enum ReturnAsEnum {
		/** Return as a list. */
		LIST,
		
		/** Return as a map having the property name as the key. */
		MAPBYNAME
	}

	// Action properties.
	/** The generic process request ID. */
	String requestId

	/** The type of collection to return. */
	ReturnAsEnum returnAs = ReturnAsEnum.MAPBYNAME

	/**
	 * Runs the action.	
	 * @return The specified type of collection.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/request/{requestId}/properties")
			.resolveTemplate("requestId", requestId)
		logDebug("target=$target")

		Object processProperties
				
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			List<UcdProperty> ucdProperties = response.readEntity(new GenericType<List<UcdProperty>>(){})
			
			if (ReturnAsEnum.LIST.equals(returnAs)) {
				processProperties = ucdProperties
			} else {
				Map<String, UcdProperty> propertiesMap = [:]
				for (ucdProperty in ucdProperties) {
					propertiesMap.put(ucdProperty.getName(), ucdProperty)
				}
				processProperties = propertiesMap
			}
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return processProperties
	}	
}
