/**
 * This action creates an agent resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdCreateAgentResource extends UcAdfAction {
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource path or ID. */
	String resource

	/** The agent name or ID. */	
	String agent
	
	/** The flag that indicates fail if the agent resource exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the agent resource was created.
	 */
	@Override
	public Object run() {
		validatePropsExist()

		Boolean created = false
				
		// If no parent path was provided then split the provided path to get the parent.
		String name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}

		String resourcePath = "${parent}/${name}"

		logVerbose("Creating agent resource [$resourcePath].")

		created = actionsRunner.runAction([
			action: UcdCreateResource.getSimpleName(),
			actionInfo: actionInfo,
			agent: agent,
			name: name,
			parent: parent,
			failIfExists: failIfExists
		])
		
		return created
	}
}
