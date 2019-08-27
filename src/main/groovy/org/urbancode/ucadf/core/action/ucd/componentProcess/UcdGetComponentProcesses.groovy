/**
 * This action gets the list of a component's processes.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdGetComponentProcesses extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/**
	 * Runs the action.	
	 * @return The list of component process objects.
	 */
	@Override
	public List<UcdComponentProcess> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdComponentProcess> ucdComponentProcesses = []
		
		logInfo("Getting component [$component] processes.")
		
		// If an component ID was provided then use it. Otherwise get the component information to get the ID.
		String componentId = component
		if (!UcdObject.isUUID(component)) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				component: component,
				failIfNotFound: true
			])
			componentId = ucdComponent.getId()
		}
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component/{componentId}/processes/active")
			.resolveTemplate("componentId", componentId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentProcesses = response.readEntity(new GenericType<List<UcdComponentProcess>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}

		return ucdComponentProcesses
	}
}
