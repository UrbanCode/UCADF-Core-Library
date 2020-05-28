/**
 * This action gets a list of environment templates.
 */
package org.urbancode.ucadf.core.action.ucd.environmentTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.applicationTemplate.UcdGetApplicationTemplate
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.environmentTemplate.UcdEnvironmentTemplate
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetEnvironmentTemplates extends UcAdfAction {
	/** The application template name or ID. */
	String applicationTemplate
	
	/** (Optional) If specified then gets environment templates with names that match this regular expression. */
	String match = ""

	/**
	 * Runs the action.	
	 * @return The list of environment template objects.
	 */
	@Override
	public List<UcdEnvironmentTemplate> run() {
		// Validate the action properties.
		validatePropsExist()

		// If an application template ID was provided then use it. Otherwise get the application template information to get the ID.
		String applicationTemplateId
		if (UcdObject.isUUID(applicationTemplate)) {
			applicationTemplateId = applicationTemplate
		} else {
			UcdApplicationTemplate ucdApplicationTemplate = actionsRunner.runAction([
				action: UcdGetApplicationTemplate.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				applicationTemplate: applicationTemplate,
				actionInfo: false
			])
			
			applicationTemplateId = ucdApplicationTemplate.getId()
		}

		List<UcdEnvironmentTemplate> ucdEnvironmentTemplates
		List<UcdEnvironmentTemplate> ucdReturnEnvironmentTemplates = []

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate/{applicationTemplateId}/-1/environmentTemplates")
			.resolveTemplate("applicationTemplateId", applicationTemplateId)
		logVerbose("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdEnvironmentTemplates = response.readEntity(new GenericType<List<UcdEnvironmentTemplate>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		if (match) {		
			for (ucdEnvironmentTemplate in ucdEnvironmentTemplates) {
				if (match && !(ucdEnvironmentTemplate.getName() ==~ match)) {
					continue
				}

				ucdReturnEnvironmentTemplates.add(ucdEnvironmentTemplate)
			}
		} else {
			ucdReturnEnvironmentTemplates = ucdEnvironmentTemplates
		}
		
		return ucdReturnEnvironmentTemplates
	}
}
