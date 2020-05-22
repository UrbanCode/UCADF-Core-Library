/**
 * This action gets a list of application template processes.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplateProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.applicationTemplate.UcdGetApplicationTemplate
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetApplicationTemplateProcesses extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. */
	String applicationTemplate

	/** The version of the template. Default is -1. */	
	Long version = -1
	
	/**
	 * Runs the action.	
	 * @return The list of application template process objects.
	 */
	@Override
	public List<UcdApplicationProcess> run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Getting application template [$applicationTemplate] processes.")
		
		// If an application template ID was provided then use it. Otherwise get the application template information to get the ID.
		String applicationTemplateId = applicationTemplate
		if (!UcdObject.isUUID(applicationTemplate)) {
			UcdApplicationTemplate ucdApplicationTemplate = actionsRunner.runAction([
				action: UcdGetApplicationTemplate.getSimpleName(),
				actionInfo: false,
				applicationTemplate: applicationTemplate,
				failIfNotFound: true
			])
			applicationTemplateId = ucdApplicationTemplate.getId()
		}
		
		List<UcdApplicationProcess> ucdApplicationProcesses = []
		
		WebTarget target 
		target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate/{applicationTemplate}/{version}/processes")
			.resolveTemplate("applicationTemplate", applicationTemplateId)
			.resolveTemplate("version", version)
		logDebug("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationProcesses = response.readEntity(new GenericType<List<UcdApplicationProcess>>(){})
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		return ucdApplicationProcesses
	}	
}
