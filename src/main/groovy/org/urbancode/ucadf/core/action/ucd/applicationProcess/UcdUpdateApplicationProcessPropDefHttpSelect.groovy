/**
 * This action updates an application process HTTP select property definition.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDefHttpSelect

import groovy.json.JsonBuilder

class UcdUpdateApplicationProcessPropDefHttpSelect extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
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
		
		logVerbose("Set application [$application] process [$process] property definition [$name].")

		// Get the application process full information.		
		UcdApplicationProcess ucdApplicationProcess = actionsRunner.runAction([
			action: UcdGetApplicationProcess.getSimpleName(),
			actionInfo: false,
			application: application,
			process: process,
			full: true
		])
		
		// Find the matching property definition name.
		UcdPropDefHttpSelect ucdPropDef = ucdApplicationProcess.getPropDefs().find {
			it.getName() == name
		}

		// If the property definition exists then start with that, otherwise initialize a new property definition object.
		if (ucdPropDef)	{
			logVerbose("Updating application [$application] process [$process] property definition [$name].")
		} else {
			logVerbose("Adding application [$application] process [$process] property definition [$name].")
			
			ucdPropDef = new UcdPropDefHttpSelect()
		}

		// Create the payload. If a value was provided then use it, otherwise use the value from the property definition object.
		Map requestMap = [
			name: name,
			description: (description != null) ? description : ucdPropDef.getDescription(),
			label: (label != null) ? label : ucdPropDef.getLabel(),
			type: ucdPropDef.getType(),
			pattern: (pattern != null) ? pattern : ucdPropDef.getPattern(),
			required: (required != null) ? required : ucdPropDef.getRequired(),
			value: ucdPropDef.getValue(),
			definitionGroupId: ucdApplicationProcess.getPropSheetDef().getId(),
			applicationProcessVersion: ucdApplicationProcess.getVersion(),
			existingId: ucdPropDef.getId(),
			httpUrl: (httpUrl != null) ? httpUrl : ucdPropDef.getHttpUrl(),
			httpAuthenticationType: (httpAuthenticationType != null) ? httpAuthenticationType : ucdPropDef.getHttpAuthenticationType(),
			httpUsername: (httpUsername != null) ? httpUsername : ucdPropDef.getHttpUsername(),
			httpPassword: (httpPassword != null) ? httpPassword.toString() : ucdPropDef.getHttpPassword(),
			httpFormat: (httpFormat != null) ? httpFormat : ucdPropDef.getHttpFormat(),
			httpBasePath: (httpBasePath != null) ? httpBasePath : ucdPropDef.getHttpBasePath(),
			httpValuePath: (httpValuePath != null) ? httpValuePath : ucdPropDef.getHttpValuePath(),
			httpLabelPath: (httpLabelPath != null) ? httpLabelPath : ucdPropDef.getHttpLabelPath(),
			resolveHttpValueUrl: "rest/deploy/applicationProcess/${ucdApplicationProcess.getId()}/propDefs/resolveHttpValues/$name}"
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationProcess/{processId}/savePropDef")
			.resolveTemplate("processId", ucdApplicationProcess.getId())
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() != 200) {
            throw new UcAdfInvalidValueException(response)
		}
	}
}
