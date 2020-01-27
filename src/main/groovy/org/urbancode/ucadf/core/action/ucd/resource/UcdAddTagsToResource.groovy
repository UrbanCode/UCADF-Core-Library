/**
 * This action adds tags to a resuorce.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

class UcdAddTagsToResource extends UcAdfAction {
	// Action properties.
	/** The resource path or ID. */
	String resource
	
	/** The list of tag names or IDs. */
	List<String> tags
	
	/** The flag that indicates that other tags should be removed. Default is false. */
	Boolean removeOthers = false
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		for (tag in tags) {		
			logVerbose("Add tag [$tag] to resource [$resource].")
	
			// Validate the resource tag exists so that it's not automatically created.
			UcdTag ucdTag = actionsRunner.runAction([
				action: UcdGetResourceTag.getSimpleName(),
				actionInfo: false,
				tag: tag
			])
	
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/tag")
				.queryParam("resource", resource)
				.queryParam("tag", tag)
			logDebug("target=$target")
	
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
			if (response.getStatus() == 204) {
				logVerbose("Tag [$tag] added to resource [$resource].")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
		
		if (removeOthers) {
			// Get the resource to get the tags.
			UcdResource ucdResource = actionsRunner.runAction([
				action: UcdGetResource.getSimpleName(),
				resource: resource
			])

			// Find tags that need to be removed.			
			List<String> removeTags = []
			for (ucdTag in ucdResource.getTags()) {
				if (!tags.contains(ucdTag.getName())) {
					removeTags.add(ucdTag.getName())
				}
			}

			// Remove the tags.
			if (removeTags.size() > 0) {
				actionsRunner.runAction([
					action: UcdRemoveTagsFromResource.getSimpleName(),
					resource: resource,
					tags: removeTags
				])
			}
		}
	}
}
