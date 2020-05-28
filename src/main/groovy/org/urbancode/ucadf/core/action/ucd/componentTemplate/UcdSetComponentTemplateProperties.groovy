/**
 * This action sets component template properties.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdSetComponentTemplateProperties extends UcAdfAction {
	// Action properties.
	/** The component template name or ID. */
	String componentTemplate

	/** The list of properties. */	
	@JsonProperty("properties")
	List<UcdProperty> ucdProperties
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// TODO: Need to determine if there's a better API to update multiple properties in one call.
		// Process each property.
		for (ucdProperty in ucdProperties) {
			logVerbose("Setting component template [$componentTemplate] property name [${ucdProperty.getName()}] secure [${ucdProperty.getSecure()}]" + (ucdProperty.getSecure() ? "." : " value [${ucdProperty.getValue()}]."))
	
			// Have to get the component template each iteration to have the correct version information.		
			UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
				action: UcdGetComponentTemplate.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				componentTemplate: componentTemplate,
				failIfNotFound: true
			])
		
			String templateId = "%26" + ucdComponentTemplate.getId() +"%26"
			WebTarget target
			
			Map requestMap = [
				name : ucdProperty.getName(), 
				description : ucdProperty.getDescription(), 
				secure : ucdProperty.getSecure(), 
				value : ucdProperty.getValue()
			]
	
			// See if the property already exists.
			UcdProperty ucdExistingProperty = ucdComponentTemplate.getPropValues().find {
				(it.getName() == ucdProperty.getName())
			}
	
			if (ucdExistingProperty) {
				// Update the property.
				target = ucdSession.getUcdWebTarget().path("/property/propSheet/componentTemplates{templateLookup}propSheet.-1/propValues/{propName}")
					.resolveTemplateFromEncoded("templateLookup", templateId)
					.resolveTemplate("propName", ucdProperty.getName())
					
				requestMap.put("existingId", ucdExistingProperty.getId())
			} else {
				// Add the property.
				target = ucdSession.getUcdWebTarget().path("/property/propSheet/componentTemplates{templateLookup}propSheet.-1/propValues")
					.resolveTemplateFromEncoded("templateLookup", templateId)
			}
			logDebug("target=$target")
	
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
			// Had to put Version in the header to avoid 409 status errors		
			Response response = target.request(MediaType.APPLICATION_JSON)
				.header("Version", ucdComponentTemplate.getVersion())
				.put(Entity.json(jsonBuilder.toString()))
				
			if (response.getStatus() == 200) {
				logVerbose("Property [${ucdProperty.getName()}] set.")
				
				// Intentionally sleeping because of UCD 6.1 problems if the component template properties are updated too quickly in succession.
				Thread.sleep(1000)
			} else {
	            throw new UcAdfInvalidValueException(response)
			}
		}
	}
}
