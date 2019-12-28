/**
 * This action removes tags from an agent.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdRemoveTagsFromAgent extends UcAdfAction {
	// Action properties.
	/** The agent name or ID. */
	String agent
	
	/** The list of tag names or IDs. */
	List<String> tags

	/** The flag that indicates to perform the removal, otherwise show that the removal would be done. Default is true. */
	Boolean commit = true

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		for (tag in tags) {
			if (commit) {
				logInfo("Remove tag [$tag] from agent [$agent].")
				
				WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentCLI/tag")
					.queryParam("agent", agent)
					.queryParam("tag", tag)
					
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 204) {
					logInfo("Tag [$tag] removed from agent [$agent].")
				} else {
		            throw new UcdInvalidValueException(response)
				}
			} else {
				logInfo("Would remove tag [$tag] from agent [$agent].")
			}
		}
	}
}
