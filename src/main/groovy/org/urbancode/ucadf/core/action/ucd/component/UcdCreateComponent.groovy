/**
 * This action creates a component.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.componentTemplate.UcdGetComponentTemplate
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentTypeEnum
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionTypeEnum

import groovy.json.JsonBuilder

class UcdCreateComponent extends UcAdfAction {
	// Action properties.
	/** The component name. */
	String name
	
	/** The component type. Default is STANDARD. */
	UcdComponentTypeEnum componentType = UcdComponentTypeEnum.STANDARD

	/** The description. Default is blank. */
	String description = ""

	/** The flag that indicates if automatic imports are to be done. Default is false. */
	Boolean importAutomatically = false
	
	/** The flag that indicates if VFS should be used for a STANDARD component type. Default is true. */
	Boolean useVfs = true

	/** Ignore qualifies for a ZOS component type. Default is 0. */
	Long ignoreQualifiers = 0
		
	/** The version type. Default is FULL. */
	UcdVersionTypeEnum defaultVersionType = UcdVersionTypeEnum.FULL
	
	/** The cleanup days to keep. Default is 0. */
	Long cleanupDaysToKeep = 0
	
	/** The cleanup cound to keep. Default is 0. */
	Long cleanupCountToKeep = 0
	
	/** The component template name or ID. Default is blank. */
	String template = ""
	
	/** The component template version. Default is blank. */
	String templateVersion = ""

	/** The sourrce configuration plugin-specific property values. Default is empty. */
	Map<String, String> properties = [:]
	
	/** The flag that indicates fail if the component already exists. Default is true. */
	Boolean failIfExists = true
		
	/**
	 * Runs the action.	
	 * @return True if the component was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		logVerbose("Creating component [$name].")

		// Get the component template ID.
		String templateId = template
		if (template && !UcdObject.isUUID(template)) {
			UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
				action: UcdGetComponentTemplate.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				componentTemplate: template
			])
			
			templateId = ucdComponentTemplate.getId()
		}

		Map requestMap = [
			componentType: componentType,
			defaultVersionType: defaultVersionType,
			description: description,
			importAutomatically: importAutomatically,
			name: name,
			properties: properties
		]

		// Additional values for STANDARD compnent type.
		if (componentType == UcdComponentTypeEnum.STANDARD) {
			requestMap.put("useVfs", useVfs)
		}

		// Additional values for ZOS component type.
		if (componentType == UcdComponentTypeEnum.ZOS) {
			requestMap.put("ignoreQualifiers", ignoreQualifiers)
		}
		
		if (cleanupCountToKeep != 0)  { requestMap.put("cleanupCountToKeep", cleanupCountToKeep) }
		if (cleanupDaysToKeep != 0)  { requestMap.put("cleanupDaysToKeep", cleanupDaysToKeep) }
		if (templateId) { requestMap.put("templateId", templateId) }
		if (templateVersion) { requestMap.put("templateVersion", templateVersion) }

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=$jsonBuilder")

		// Create the component.
        WebTarget target = ucdSession.getUcdWebTarget().path("/cli/component/create")
        logDebug("target=$target")

        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
			logVerbose("Component created.")
			created = true
        } else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			logVerbose(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcAdfInvalidValueException(errMsg)
			}
        }
		
		return created
	}
}
