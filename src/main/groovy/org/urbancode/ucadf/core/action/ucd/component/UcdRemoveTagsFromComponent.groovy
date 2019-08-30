/**
 * This action removes tags from components.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdRemoveTagsFromComponent extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The list of tag names or IDs. */
	List<String> tags
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		for (tag in tags) {
			logInfo("Remove tag [$tag] from component [$component].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/tag")
				.queryParam("component", component)
				.queryParam("tag", tag)
				
			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logInfo("Tag [$tag] removed from component [$component].")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
