/**
 * This action creates a group resource.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import groovy.json.JsonBuilder

class UcdCreateGroupResource extends UcAdfAction {
	/** (Optional) The parent path or ID. */
	String parent = ""
	
	/** The resource path or ID. */
	String resource

	/** (Optional) The description. */	
	String description = ""
	
	/** The flag that indicates fail if the group resource already exists. Default is true. */
	Boolean failIfExists = true

	// Private properties.
	private String name
	private Boolean created = false
	
	/**
	 * Runs the action.	
	 * @return True if the group resource was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		// If no parent path was provided then split the provided path to get the parent.
		name = resource
		if (!parent) {
			(parent, name) = UcdResource.getParentPathAndName(resource)
		}

		logVerbose("Creating group resource [$parent/$name].")
		
		// See if the resource already exists.
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/info")
			.queryParam("resource", "${parent}/${name}")
		logDebug("target=$target")

		Response response = target.request().get()
	
		if (response.getStatus() == 200) {
			String message = "Resource [${parent}/${name}] already exists."
			if (failIfExists) {
				throw new UcAdfInvalidValueException(message)
			} else {
				logVerbose(message)
			}
		} else {
			createResourceWithRetry()
			created = true
		}
		
		return created
	}

	// Logic to handle concurrency issues.
	public createResourceWithRetry() {
		Boolean uniqueResultError
		Boolean alreadyExists
		final Integer MAXATTEMPTS = 20
		for (Integer iAttempt = 1; iAttempt <= MAXATTEMPTS; iAttempt++) {
			// Attempt to create the resourece.
			(alreadyExists, uniqueResultError) = createResource()
			if (alreadyExists) {
				break
			}
			
			if (!uniqueResultError) {
				// Attempt to retrieve the newly created resource.
				uniqueResultError = retrieveNewResource()
			}

			// If no error then quit.
			if (!uniqueResultError) {
				break
			}

			logVerbose("A unique result error was encountered on attempt $iAttempt. Trying again.")

			// Attempt to delete the resource.
			actionsRunner.runAction([
				action: UcdDeleteResource.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				resource: resource
			])

			// Sleep for a random period of time.
			Random rand = new Random(System.currentTimeMillis())
			Thread.sleep(rand.nextInt(2000))
		}
		
		if (uniqueResultError) {
			throw new UcAdfInvalidValueException("Errors were encountered while trying to create resource.")
		}
	}	
	
	// Attempt to create the resource.	
	public Object createResource() {
		Boolean uniqueResultError = false
		Boolean alreadyExists = false
		
		// Construct the request map.
		Map<String, String> requestMap = [
			name: name,
			parent: parent,
			description: description
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=${jsonBuilder.toString()}")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/create")
		logDebug("target=$target")

		Response response = target.request(MediaType.APPLICATION_JSON)
			.put(Entity.json(jsonBuilder.toString()))
			
		if (response.getStatus() == 200) {
			created = true
		} else {
			String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
			if (response.getStatus() == 400 && errMsg.matches(/.*already exists.*/)) {
				if (failIfExists) {
					throw new UcAdfInvalidValueException(errMsg)
				} else {
					logVerbose("Resource [$parent/$name] already exists.")
					alreadyExists = true
				}
			} else if (response.getStatus() == 400 && errMsg.matches(/.*query did not return a unique result.*/)) {
				uniqueResultError = true
			} else {
				throw new UcAdfInvalidValueException(errMsg)
			}
		}
		
		return [alreadyExists, uniqueResultError]
	}

	// Attempt to retrieve the newly created resource.
	public Boolean retrieveNewResource() {
		Boolean uniqueResultError = false
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/resource/info")
			.queryParam("resource", "${parent}/${name}")
			
		logDebug("target=$target")
	
		Response response = target.request().get()
		
		// If the get encounters a unique result error then delete the resource and create it again.
		if (response.getStatus() != 200) {
			logWarn("Attempt to retrieve resource returned ${response.getStatus()}.")
			uniqueResultError = true
		}
		
		return uniqueResultError
	}
}
