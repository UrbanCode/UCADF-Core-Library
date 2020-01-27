/**
 * This action gets a component template.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetComponentTemplate extends UcAdfAction {
	// Action properties.
	/** The component template name or ID. */
	String componentTemplate
	
	/** The flag that indicates fail if the component template is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The component template object.
	 */
	@Override
	public UcdComponentTemplate run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting component template [$componentTemplate].")

		UcdComponentTemplate ucdComponentTemplate
		
		// If an component template ID was provided then use it. Otherwise get the component template information to get the ID.
		String componentTemplateId
		if (UcdObject.isUUID(componentTemplate)) {
			componentTemplateId = componentTemplate
		} else {
			// No API found to get a single template by name so have to get the list of all templates then select the one from it.
			List<UcdComponentTemplate> ucdComponentTemplates = actionsRunner.runAction([
				action: UcdGetComponentTemplates.getSimpleName(),
				actionInfo: false
			])
	
			UcdComponentTemplate ucdFindComponentTemplate = ucdComponentTemplates.find {
				(it.getName() == componentTemplate)
			}
		
			if (ucdFindComponentTemplate) {
				componentTemplateId = ucdFindComponentTemplate.getId()
			} else {
				if (failIfNotFound) {
					throw new UcdInvalidValueException("Component template [$componentTemplate] not found.")
				}
			}
		}
		
		// Get the component template details.
		if (componentTemplateId) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentTemplate/{componentTemplateId}/-1")
				.resolveTemplate("componentTemplateId", componentTemplateId)
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdComponentTemplate = response.readEntity(UcdComponentTemplate.class)
			} else {
				String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() == 404 || response.getStatus() == 403) {
					if (failIfNotFound) {
						throw new UcdInvalidValueException(errMsg)
					}
				} else {
					throw new UcdInvalidValueException(errMsg)
				}
			}
		}

		return ucdComponentTemplate
	}
}
