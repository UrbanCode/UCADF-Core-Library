/**
 * This action gets an environment template.
 */
package org.urbancode.ucadf.core.action.ucd.environmentTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.environmentTemplate.UcdEnvironmentTemplate
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetEnvironmentTemplate extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. */
	String applicationTemplate
	
	/** The environment template name or ID. */
	String environmentTemplate
	
	/** The flag that indicates fail if the environment template is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.
	 * @return The environment template object.
	 */
	public UcdEnvironmentTemplate run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting environment template [$environmentTemplate].")

		UcdEnvironmentTemplate ucdEnvironmentTemplate
		
		// If an environment template ID was provided then use it. Otherwise get the environment template information to get the ID.
		String environmentTemplateId
		if (UcdObject.isUUID(environmentTemplate)) {
			environmentTemplateId = environmentTemplate
		} else {
			// No API found to get a single template by name so have to get the list of all templates then select the one from it.
			List<UcdEnvironmentTemplate> ucdEnvironmentTemplates = actionsRunner.runAction([
				action: UcdGetEnvironmentTemplates.getSimpleName(),
				applicationTemplate: applicationTemplate,
				actionInfo: false
			])

			UcdEnvironmentTemplate ucdFindEnvironmentTemplate = ucdEnvironmentTemplates.find {
				(it.getName() == environmentTemplate)
			}
		
			if (ucdFindEnvironmentTemplate) {
				environmentTemplateId = ucdFindEnvironmentTemplate.getId()
			} else {
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException("Environment template [$environmentTemplate] not found.")
				}
			}
		}
		
		// Get the environment template details.
		if (environmentTemplateId) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/environmentTemplate/{environmentTemplateId}/-1")
				.resolveTemplate("environmentTemplateId", environmentTemplateId)
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdEnvironmentTemplate = response.readEntity(UcdEnvironmentTemplate.class)
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

		return ucdEnvironmentTemplate
	}
}
