/**
 * This action gets a component template's processes.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplateProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.componentTemplate.UcdGetComponentTemplate
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetComponentTemplateProcesses extends UcAdfAction {
	// Action properties.
	/** The component template name or ID. */
	String componentTemplate
	
	/**
	 * Runs the action.	
	 * @return The list of component process objects.
	 */
	@Override
	public List<UcdComponentProcess> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdComponentProcess> ucdComponentProcesses = []
		
		logInfo("Getting component template [$componentTemplate] processes.")

		// If an component template ID was provided then use it. Otherwise get the component information to get the ID.
		String componentTemplateId = componentTemplate
		if (!UcdObject.isUUID(componentTemplate)) {
			UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
				action: UcdGetComponentTemplate.getSimpleName(),
				componentTemplate: componentTemplate,
				failIfNotFound: true
			])
			componentTemplateId = ucdComponentTemplate.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentTemplate/{compTemplateId}/-1/processes/active")
			.resolveTemplate("compTemplateId", componentTemplateId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentProcesses = (response.readEntity(new GenericType<List<UcdComponentProcess>>(){})).findAll { it.getPath() ==~ /^componentTemplates.*/ }
		} else {
            throw new UcdInvalidValueException(response)
		}

		return ucdComponentProcesses
	}
}
