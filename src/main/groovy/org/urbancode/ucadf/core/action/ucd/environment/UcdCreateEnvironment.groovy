/**
 * This action creates an environment.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.application.UcdGetApplication
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.general.UcdColorEnum
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import groovy.json.JsonBuilder

class UcdCreateEnvironment extends UcAdfAction {
	// Action properites.
	/** The application name or ID. */
	String application

	/** The name. */	
	String name
	
	/** The color. */
	UcdColorEnum color
	
	/** (Optional) The description. */
	String description = ""
	
	/** The flag that indicates require approvals. Default is false. */
	Boolean requireApprovals = false
	
	/** The flag that indicates require snapshot. Default is false. */
	Boolean requireSnapshot = false
	
	/** The flag that indicates require snapshot. Default is false. */
	Boolean noSelfApprovals = false
	
	/** The flag that indicates require snapshot. Default is false. */
	Boolean lockSnapshots = false
				
	/** The flag that indicates fail if the environment already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the environment was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
        logVerbose("Creating application [$application] environment [$name].")

		// Determine whether to use new or old APIs.
		if (requireSnapshot || noSelfApprovals || lockSnapshots) {
			// If an application ID was provided then use it. Otherwise get the application information to get the ID.
			String applicationId = application
			if (!UcdObject.isUUID(application)) {
				UcdApplication ucdApplication = actionsRunner.runAction([
					action: UcdGetApplication.getSimpleName(),
					actionInfo: false,
					actionVerbose: false,
					application: application,
					failIfNotFound: true
				])

				applicationId = ucdApplication.getId()
			}
	
			// Initialize the request.
			Map requestMap = [
	            applicationId: applicationId,
	            name: name,
	            description: description,
	            color: color.getValue(),
	            requireApprovals: requireApprovals,
				requireSnapshot: requireSnapshot,
				noSelfApprovals: noSelfApprovals,
				lockSnapshots: lockSnapshots
			]
			
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			logDebug("jsonBuilder=$jsonBuilder")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/environment")
			logDebug("target=$target")
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
	        if (response.getStatus() == 200) {
	            logVerbose("Application [$application] environment [$name] created.")
				created = true
	        } else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
					throw new UcAdfInvalidValueException(errMsg)
				}
	        }
		} else {
			// Validate the environment doesn't already exist.
			if (failIfExists) {
				UcdEnvironment ucdEnvironment = actionsRunner.runAction([
					action: UcdGetEnvironment.getSimpleName(),
					actionInfo: false,
					actionVerbose: false,
					application: application,
					environment: name,
					failIfNotFound: false
				])
				
				if (ucdEnvironment) {
					throw new UcAdfInvalidValueException("Application [$application] environment [$name] already exists.")
				}
			}
		
			// The older CLI API.
	        WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/createEnvironment")
	            .queryParam("application", application)
	            .queryParam("name", name)
	            .queryParam("description", description)
	            .queryParam("color", color.getValue())
	            .queryParam("requireApprovals", requireApprovals)
	        logDebug("target=$target")
	
	        Response response = target.request(MediaType.WILDCARD).put(Entity.json(""))
	        // UCD 6.2 returns 200 and UCD 6.1 returns 201. Returns 200 if the environment already exists.
	        if (response.getStatus() == 200 || response.getStatus() == 201) {
	            logVerbose("Application [$application] environment [$name] created.")
				created = true
	        } else {
	            throw new UcAdfInvalidValueException(response)
	        }
		}
		
		return created
	}
}
