/**
 * This action gets an application template.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetApplicationTemplate extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. */
	String applicationTemplate
	
	/** The flag that indicates fail if the application template is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.
	 * @return The application template object.
	 */
	public UcdApplicationTemplate run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting application template [$applicationTemplate].")

		UcdApplicationTemplate ucdApplicationTemplate
		
		// If an application template ID was provided then use it. Otherwise get the application template information to get the ID.
		String applicationTemplateId
		if (UcdObject.isUUID(applicationTemplate)) {
			applicationTemplateId = applicationTemplate
		} else {
			// No API found to get a single template by name so have to get the list of all templates then select the one from it.
			List<UcdApplicationTemplate> ucdApplicationTemplates = actionsRunner.runAction([
				action: UcdGetApplicationTemplates.getSimpleName(),
				actionInfo: false
			])
	
			UcdApplicationTemplate ucdFindApplicationTemplate = ucdApplicationTemplates.find {
				(it.getName() == applicationTemplate)
			}
		
			if (ucdFindApplicationTemplate) {
				applicationTemplateId = ucdFindApplicationTemplate.getId()
			} else {
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException("Application template [$applicationTemplate] not found.")
				}
			}
		}
		
		// Get the application template details.
		if (applicationTemplateId) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate/{applicationTemplateId}/-1")
				.resolveTemplate("applicationTemplateId", applicationTemplateId)
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdApplicationTemplate = response.readEntity(UcdApplicationTemplate.class)
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() == 404 || response.getStatus() == 403) {
					if (failIfNotFound) {
						throw new UcAdfInvalidValueException(errMsg)
					}
				} else {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}

		return ucdApplicationTemplate
	}
}
