/**
 * This action creates a component resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdCreateComponentResource extends UcAdfAction {
	// Action properties.
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource path or ID. */
	String resource
	
	/** The component name or ID. */
	String component
	
	/** The flag that indicates fail if the component resource already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the resource was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean created = false
		
		String name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}
		
		String resourcePath = "${parent}/${name}"
		
		logVerbose("Creating component resource [$resourcePath].")

		// Look up the role ID and use it instead of the name because of an UrbanCode that
		// sometimes incorrectly corrects the resource with the wrong role ID.
		UcdComponent ucdComponent = actionsRunner.runAction([
			action: UcdGetComponent.getSimpleName(),
			actionInfo: false,
			component: component
		])
		
		if (!ucdComponent) {
			throw new UcAdfInvalidValueException("Component [$component] not found.")
		}

		String roleId = ucdComponent.getResourceRole().get("id")

		// Create the component resource.
		created = actionsRunner.runAction([
			action: UcdCreateResource.getSimpleName(),
			actionInfo: actionInfo,
			name: name,
			parent: parent,
			role: roleId,
			failIfExists: failIfExists
		])
		
		return created
	}
}
