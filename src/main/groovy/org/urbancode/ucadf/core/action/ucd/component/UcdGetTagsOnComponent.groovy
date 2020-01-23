/**
 * This action gets the tags on a component.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

class UcdGetTagsOnComponent extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/**
	 * Runs the action.	
	 * @return The list of tag objects.
	 */
	@Override
	public List<UcdTag> run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting tags on component [$component].")
	
		List<UcdTag> ucdTags = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/tag")
			.queryParam("component", component)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdTags = response.readEntity(new GenericType<List<UcdTag>>(){})
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (response.getStatus() != 404) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return ucdTags
	}
}
