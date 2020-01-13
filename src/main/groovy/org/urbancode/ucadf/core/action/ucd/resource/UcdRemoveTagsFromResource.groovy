/**
 * This action removes tags from a resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdRemoveTagsFromResource extends UcAdfAction {
	// Action properties.
	/** The resource path or ID. */
	String resource
	
	/** The list of tags. */
	List<String> tags
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		for (tag in tags) {
			logInfo("Remove tag [$tag] from resource [$resource].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/tag")
				.queryParam("resource", resource)
				.queryParam("tag", tag)
			logDebug("target=$target")

			Response response = target.request(MediaType.APPLICATION_JSON).delete()
			if (response.getStatus() == 204) {
				logInfo("Tag [$tag] removed from resource [$resource].")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
