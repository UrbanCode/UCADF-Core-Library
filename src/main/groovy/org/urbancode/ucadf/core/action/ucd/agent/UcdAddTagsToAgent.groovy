/**
 * This action adds tags to an agent.
 */
package org.urbancode.ucadf.core.action.ucd.agent

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdAddTagsToAgent extends UcAdfAction {
	// Action properties.
	/** The agent name or ID. */
	String agent
	
	/** The list of tag names or IDs to add. */
	List<String> tags

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		for (tag in tags) {
			logVerbose("Add tag [$tag] to agent [$agent].")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/agentCLI/tag")
				.queryParam("agent", agent)
				.queryParam("tag", tag)
			logDebug("target=$target")
				
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
			if (response.getStatus() == 204) {
				logVerbose("Tag [$tag] added to agent [$agent].")
			} else {
				throw new UcdInvalidValueException(response)
			}
		}
	}
}
