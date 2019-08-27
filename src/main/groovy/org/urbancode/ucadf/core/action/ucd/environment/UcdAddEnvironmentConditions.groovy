/**
 * This action adds environment conditions.
 * Each first level list item is an OR condition against each other first list item and the statuses in each sublist are AND conditions with each other.
 * The follow example evaluates as: (status1 and status2) or (status3)
 * conditions:
 *   -
 *     - "status1"
 *     - "status2"
 *   -
 *     - "status3"
*/ 
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import groovy.json.JsonBuilder

class UcdAddEnvironmentConditions extends UcAdfAction {
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment

	/** The list of conditions. */	
	List conditions
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		logInfo("Adding conditions to application [$application] environment[$environment].")

		// Get the environment information.
		UcdEnvironment ucdEnvironment = actionsRunner.runAction([
			action: UcdGetEnvironment.getSimpleName(),
			application: application,
			environment: environment,
			withDetails: true
		])

		List requestMap = [
			[
				environmentId: ucdEnvironment.getId(),
				conditions: conditions
			]
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logInfo("Add conditions request:\n$jsonBuilder")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/application/{applicationId}/environmentConditions")
			.resolveTemplate("applicationId", ucdEnvironment.getApplication().getId())
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 204) {
			logInfo("Conditions added to environment.")
		} else {
            throw new UcdInvalidValueException(response)
		}
	}
}
