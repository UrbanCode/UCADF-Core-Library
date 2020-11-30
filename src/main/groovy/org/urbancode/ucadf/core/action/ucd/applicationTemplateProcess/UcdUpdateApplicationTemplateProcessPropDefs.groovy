/**
 * This action updates an application template process property definitions.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplateProcess

import org.urbancode.ucadf.core.action.ucd.applicationProcess.UcdUpdateApplicationProcessPropDefs
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef

class UcdUpdateApplicationTemplateProcessPropDefs extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. */
	String applicationTemplate = ""
	
	/** The process name or ID. */
	String process

	/** The property definitions. */
	List<UcdPropDef> propDefs

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistInclude()
		
		// Get the application template process information.		
		UcdApplicationProcess ucdApplicationProcess = actionsRunner.runAction([
			action: UcdGetApplicationTemplateProcess.getSimpleName(),
			actionInfo: false,
			applicationTemplate: applicationTemplate,
			process: process
		])

		// Use the update application process action to update the application template process.
		actionsRunner.runAction([
			action: UcdUpdateApplicationProcessPropDefs.getSimpleName(),
			actionInfo: false,
			process: ucdApplicationProcess.getId(),
			propDefs: propDefs
		])
	}
}
