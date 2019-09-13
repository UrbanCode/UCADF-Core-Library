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
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionTypeEnum

import groovy.json.JsonBuilder

class UcdCreateComponent extends UcAdfAction {
	// Action properties.
	/** The component name. */
	String name
	
	/** (Optional) The description. */
	String description = ""
	
	/** The flag that indicates if automatic imports are to be done. Default is false. */
	Boolean importAutomatically = false
	
	/** The flag that indicates if VFS should be used. Default is true. */
	Boolean useVfs = true
	
	/** (Optional) The version type. Default is full. */
	UcdVersionTypeEnum defaultVersionType = UcdVersionTypeEnum.FULL
	
	/** (Optional) The cleanup days to keep. Default is 0. */
	Long cleanupDaysToKeep = 0
	
	/** (Optional) The cleanup cound to keep. Default is 0. */
	Long cleanupCountToKeep = 0
	
	/** (Optional) The component template name or ID. */
	String template = ""
	
	/** (Optional) The component template version. */
	String templateVersion = ""

	/** (Optional) The sourrce configuration plugin-specific property values. */
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
		
		logInfo("Creating component [$name].")

		// Get the component template ID.
		String templateId = template
		if (template && !UcdObject.isUUID(template)) {
			UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
				action: UcdGetComponentTemplate.getSimpleName(),
				componentTemplate: template
			])
			
			templateId = ucdComponentTemplate.getId()
		}

		Map requestMap = [
			defaultVersionType: defaultVersionType,
			description: description,
			importAutomatically: importAutomatically,
			name: name,
			properties: properties,
			useVfs: useVfs
		]

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
			logInfo("Component created.")
			created = true
        } else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
        }
		
		return created
	}
}
