/**
 * This action removes a component from an application.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

import groovy.json.JsonBuilder

class UcdRemoveComponentFromApplication extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application

	/** The component name or ID. */	
	String component
	
	/** The flag that indicates fail if the application or component are not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("Removing component [$component] from application [$application].")
		
		UcdApplication ucdApplication = actionsRunner.runAction([
			action: UcdGetApplication.getSimpleName(),
			actionInfo: false,
			application: application,
			failIfNotFound: failIfNotFound
		])
		
		if (ucdApplication) {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				actionInfo: false,
				component: component,
				failIfNotFound: failIfNotFound
			])
			
			if (ucdComponent) {
				Map requestMap = [
					components: [ ucdComponent.getId() ]
				]

				JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
				
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/application/{applicationId}/removeComponents")
					.resolveTemplate("applicationId", ucdApplication.getId())
				logDebug("target=$target")
				
				Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
				if (response.getStatus() == 200) {
					logVerbose("Component [$component] removed from application [$application].")
				} else {
					throw new UcAdfInvalidValueException(response)
				}
			}
		}
	}
}
