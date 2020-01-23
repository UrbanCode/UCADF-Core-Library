/**
 * This action gets an agent tag object.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

class UcdGetAgentTag extends UcAdfAction {
	// Action properties.
	/** The tag name or ID. */
	String tag
	
	/** The flag that indicates fail if the agent tag is not found. Default is true. */
	Boolean failIfNotFound = true

	/**
	 * Runs the action.	
	 * @return Returns the agent tag object.
	 */
	public UcdTag run() {
		// Validate the action properties.
		validatePropsExist()

		UcdTag ucdTag
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/tag/type/Agent/name/{tag}")
			.resolveTemplate("tag", tag)
			
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdTag = response.readEntity(UcdTag.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 400 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}

		return ucdTag
	}	
}
