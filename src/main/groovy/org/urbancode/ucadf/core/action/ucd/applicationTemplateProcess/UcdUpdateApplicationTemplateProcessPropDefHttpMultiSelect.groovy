/**
 * This action updates an application template process HTTP multi-select property definition.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplateProcess

import org.urbancode.ucadf.core.action.ucd.applicationProcess.UcdUpdateApplicationProcessPropDefHttpMultiSelect
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess

class UcdUpdateApplicationTemplateProcessPropDefHttpMultiSelect extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. */
	String applicationTemplate
	
	/** The process name or ID. */
	String process

	/** The property property name or ID. */	
	String name
	
	/** (Optional) The property definition description. */
	String description
	
	/** (Optional) The property definition label. */
	String label
	
	/** If true then the property is required. Default is false. */
	Boolean required
	
	/** (Optional) The property definition pattern. */
	String pattern

	/** (Optional) The property definition HTTP URL. */
	String httpUrl
	
	/** (Optional) The authentication type. */
	String httpAuthenticationType
		
	/** (Optional) The property definition HTTP user name. */
	String httpUsername
	
	/** (Optional) The property definition HTTP password. */
	UcAdfSecureString httpPassword
	
	/** (Optional) The property definition HTTP format. */
	String httpFormat
	
	/** (Optional) The property definition HTTP base path. */
	String httpBasePath
	
	/** (Optional) The property definition HTTP value path. */
	String httpValuePath
	
	/** (Optional) The property definition label path. */
	String httpLabelPath

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistInclude(
			[
				"application",
				"process",
				"name"
			]
		)
		
		logVerbose("Set application template [$applicationTemplate] process [$process] HTTP multi-select property definition [$name].")

		// Get the application template process information.		
		UcdApplicationProcess ucdApplicationProcess = actionsRunner.runAction([
			action: UcdGetApplicationTemplateProcess.getSimpleName(),
			actionInfo: false,
			applicationTemplate: applicationTemplate,
			process: process
		])

		// Use the update application process action to update the application template process.
		actionsRunner.runAction([
			action: UcdUpdateApplicationProcessPropDefHttpMultiSelect.getSimpleName(),
			actionInfo: false,
			process: ucdApplicationProcess.getId(),
			name: name,
			description: description,
			label: label,
			required: required,
			pattern: pattern,
			httpUrl: httpUrl,
			httpAuthenticationType: httpAuthenticationType,
			httpUsername: httpUsername,
			httpPassword: httpPassword,
			httpFormat: httpFormat,
			httpBasePath: httpBasePath,
			httpValuePath: httpValuePath,
			httpLabelPath: httpLabelPath
		])
	}
}
