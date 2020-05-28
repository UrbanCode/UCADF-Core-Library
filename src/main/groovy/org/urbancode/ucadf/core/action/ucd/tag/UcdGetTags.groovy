/**
 * This action returns a list of tags.
 */
package org.urbancode.ucadf.core.action.ucd.tag

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag
import org.urbancode.ucadf.core.model.ucd.tag.UcdTagTypeEnum

class UcdGetTags extends UcAdfAction {
	/** (Optional) The tag type to find. */
	UcdTagTypeEnum type
	
	/**
	 * Runs the action.	
	 * @return The list of tag objects.
	 */
	@Override
	public List<UcdTag> run() {
		// Validate the action properties.
		validatePropsExistExclude([ "type" ])

		List<UcdTag> ucdTags = []
		
		WebTarget target
		if (type) {
			// Add the tag type parameter.
			target = ucdSession.getUcdWebTarget().path("/rest/tag/type/{type}")
				.resolveTemplate("type", type.getValue())
		} else {
			target = ucdSession.getUcdWebTarget().path("/rest/tags")
		}
		
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdTags = response.readEntity(new GenericType<List<UcdTag>>(){})
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		return ucdTags
	}
}
