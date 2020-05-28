/**
 * This action gets a list of component configuration templates.
 */
package org.urbancode.ucadf.core.action.ucd.componentConfigTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response
import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentConfigTemplate
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetComponentConfigTemplates extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/**
	 * Runs the action.	
	 * @return The list of component configuration template objects.
	 */
	@Override
	public List<UcdComponentConfigTemplate> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdComponentConfigTemplate> ucdComponentConfigTemplates = []
		
		logVerbose("Getting component [$component] configuration templates.")

		// If a component ID was provided then use it. Otherwise get the component information to get the ID.
		String componentId = component
		if (!UcdObject.isUUID(component)) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				failIfNotFound: true
			])

			componentId = ucdComponent.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component/{componentId}/configTemplates")
			.resolveTemplate("componentId", componentId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentConfigTemplates = response.readEntity(new GenericType<List<UcdComponentConfigTemplate>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		return ucdComponentConfigTemplates
	}	
}
