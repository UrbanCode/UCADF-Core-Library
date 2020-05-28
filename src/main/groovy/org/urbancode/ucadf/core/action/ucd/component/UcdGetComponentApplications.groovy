/**
 * This action gets the list of applications associated with a component.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetComponentApplications extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/**
	 * Runs the action.	
	 * @return The list of application objects.
	 */
	@Override
	public List<UcdApplication> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdApplication> ucdApplications = []
		
		logVerbose("Getting component [$component] applications.")

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
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component/{componentId}/applications")
			.resolveTemplate("componentId", componentId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplications = response.readEntity(new GenericType<List<UcdApplication>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		return ucdApplications
	}
}
