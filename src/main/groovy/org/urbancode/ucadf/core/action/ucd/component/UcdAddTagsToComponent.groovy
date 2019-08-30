/**
 * This action adds tags to a component.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

class UcdAddTagsToComponent extends UcAdfAction {
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
			logInfo("Add tag [$tag] to component [$component].")
	
			// Validate the component tag exists so that it's not automatically created.
			UcdTag ucdTag = actionsRunner.runAction([
				action: UcdGetComponentTag.getSimpleName(),
				tag: tag
			])
	
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/tag")
				.queryParam("component", component)
				.queryParam("tag", tag)
			logDebug("target=$target")
	
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
			if (response.getStatus() == 204) {
				logInfo("Tag [$tag] added to component [$component].")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
