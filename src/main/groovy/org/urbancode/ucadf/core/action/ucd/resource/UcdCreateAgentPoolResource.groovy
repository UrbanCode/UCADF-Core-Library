/**
 * This action creates an agent pool resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdCreateAgentPoolResource extends UcAdfAction {
	// Action properties.
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource path or ID. */
	String resource

	/** The pool name or ID. */	
	String pool
	
	/** (Optional) The description. */
	String description = ""

	/** The flag that indicates fail if the agent pool resource already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the agent pool resource was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean created = false

		// If no parent path was provided then split the provided path to get the parent.
		String name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}

		String resourcePath = "${parent}/${name}"

		logInfo("Creating agent pool resource [$resourcePath].")

		created = actionsRunner.runAction([
			action: UcdCreateResource.getSimpleName(),
			agentPool: pool,
			name: name,
			parent: parent,
			description: description,
			failIfExists: failIfExists
		])
		
		return created
	}
}
