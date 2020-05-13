/**
 * This action deletes an application template.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.applicationTemplate.UcdGetApplicationTemplate
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdDeleteApplicationTemplate extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. */
	String applicationTemplate

	/** The flag that indicates fail if the application template is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/**
	 * Runs the action.
	 * @return True if the application template was deleted.
	 */
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		logVerbose("Deleting application template [$applicationTemplate] commit [$commit].")

		if (commit) {
			// If a application template ID was provided then use it. Otherwise get the application template information to get the ID.
			String applicationTemplateId
			if (UcdObject.isUUID(applicationTemplate)) {
				applicationTemplateId = applicationTemplate
			} else {
				UcdApplicationTemplate ucdApplicationTemplate = actionsRunner.runAction([
					action: UcdGetApplicationTemplate.getSimpleName(),
					actionInfo: false,
					applicationTemplate: applicationTemplate,
					failIfNotFound: failIfNotFound
				])
				
				if (ucdApplicationTemplate) {
					applicationTemplateId = ucdApplicationTemplate.getId()
				}
			}

			if (applicationTemplateId) {
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate/{compoonentTemplateId}")
					.resolveTemplate("compoonentTemplateId", applicationTemplateId)
				logDebug("target=$target")
				
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 204) {
					logVerbose("Application template [$applicationTemplate] deleted.")
					deleted = true
				} else {
					String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
					logVerbose(errMsg)
					if (response.getStatus() != 404 || failIfNotFound) {
						throw new UcAdfInvalidValueException(errMsg)
					}
				}
			}
		} else {
			logVerbose("Would delete application template [$applicationTemplate].")
		}
		
		return deleted
	}
}
