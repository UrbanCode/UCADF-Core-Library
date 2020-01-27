/**
 * This action deletes a component template.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdDeleteComponentTemplate extends UcAdfAction {
	// Action properties.
	/** The component template name or ID. */
	String componentTemplate
	
	/** The flag that indicates fail if the component template is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true

	/**
	 * Runs the action.	
	 * @return True if the component template was deleted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		logVerbose("Deleting component template [$componentTemplate] commit [$commit].")

		if (commit) {
			// If a component template ID was provided then use it. Otherwise get the component template information to get the ID.
			String componentTemplateId
			if (UcdObject.isUUID(componentTemplate)) {
				componentTemplateId = componentTemplate
			} else {
				UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
					action: UcdGetComponentTemplate.getSimpleName(),
					componentTemplate: componentTemplate,
					failIfNotFound: failIfNotFound
				])
				
				if (ucdComponentTemplate) {
					componentTemplateId = ucdComponentTemplate.getId()
				}
			}

			if (componentTemplateId) {
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentTemplate/{compoonentTemplateId}")
					.resolveTemplate("compoonentTemplateId", componentTemplateId)
				logDebug("target=$target")
				
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 204) {
					logVerbose("Component template [$componentTemplate] deleted.")
					deleted = true
				} else {
					String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
					logVerbose(errMsg)
					if (response.getStatus() != 404 || failIfNotFound) {
						throw new UcdInvalidValueException(errMsg)
					}
				}
			}
		} else {
			logVerbose("Would delete component template [$componentTemplate].")
		}
		
		return deleted
	}
}
