/**
 * This action gets a resource tag.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

class UcdGetResourceTag extends UcAdfAction {
	// Action properties.
	/** The tag name or ID. */
	String tag
	
	/** The flag that indicates fail if the resource tag is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The resource tag object.
	 */
	@Override
	public UcdTag run() {
		// Validate the action properties.
		validatePropsExist()

		UcdTag ucdTag
		
		logInfo("Getting resource tag [$tag].")
	
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/tag/type/Resource/name/{tag}")
			.resolveTemplate("tag", tag)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdTag = response.readEntity(UcdTag.class)
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (response.getStatus() != 400 || failIfNotFound) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdTag
	}
}
