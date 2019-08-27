/**
 * This action creates a group resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdCreateGroupResource extends UcAdfAction {
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource path or ID. */
	String resource

	/** (Optional) The description. */	
	String description = ""
	
	/** The flag that indicates fail if the group resource already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the group resource was created.
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		// If no parent path was provided then split the provided path to get the parent.
		String name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}

		String resourcePath = "${parent}/${name}"

		logInfo("Creating group resource [$resourcePath].")

		created = actionsRunner.runAction([
			action: UcdCreateResource.getSimpleName(),
			name: name,
			parent: parent,
			description: description,
			failIfExists: failIfExists
		])
		
		return created
	}
}
