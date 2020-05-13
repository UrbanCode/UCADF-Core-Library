/**
 * This action runs a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcessRequest

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.genericProcess.UcdGetGenericProcess
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.genericProcessRequest.UcdGenericProcessRequest
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdRunGenericProcess extends UcAdfAction {
	// Action properties.
	/** The generic process name or ID. */
	String process
	
	/** (Optional) The list of resource paths or IDs. */
	List<String> resources = []
	
	/** (Optional) The list of generic process request properties. */
	@JsonProperty("properties")
	List<UcdProperty> ucdProperties = []
	
	/** The flag that indicates to wait for the process to complete. Default is true. */
	Boolean waitForProcess = true
	
	/** The number of seconds to wait between each check. */
	Integer waitIntervalSecs = 3
	
	/** The maximum number of seconds to wait. Default is 600. */
	Integer maxWaitSecs = 600
	
	/** If true then then an exception will be thrown if the process fails. Default is true. */
	Boolean throwException = true

	/**
	 * Runs the action.	
	 * @return The list of generic process request objects.
	 */
	@Override
	public List<UcdGenericProcessRequest> run() {
		// Validate the action properties.
		validatePropsExist()
		
		List<UcdGenericProcessRequest> ucdGenericProcessRequests = []
		
		logVerbose("Running generic process [$process].")

		// Get the generic process information.
		UcdGenericProcess ucdGenericProcess = actionsRunner.runAction([
			action: UcdGetGenericProcess.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			process: process,
			failIfNotFound: true
		])

		// Build a custom post body that includes only the required fields.
		Map processPropertiesMap = [:]
		
		for (ucdProperty in ucdProperties) {
			processPropertiesMap.put(ucdProperty.getName(), ucdProperty.getValue())
		}

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/request")
		logDebug("target=$target")
		
		// If no resource or resources were specified then add the default.
		if (resources.size() == 0) {
			resources.add(ucdGenericProcess.getDefaultResourceId())
		}
	
		// Construct the request.
		Map requestMap

		if (ucdSession.compareVersion(UcdSession.UCDVERSION_704) >= 0) {
			requestMap = [
				processId: ucdGenericProcess.getId(),
				resources: resources,
				properties: processPropertiesMap
			]
			
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
	
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				ucdGenericProcessRequests = response.readEntity(new GenericType<List<UcdGenericProcessRequest>>(){})
			} else {
				String errMsg = response.readEntity(String.class)
				logVerbose(errMsg)
				if (throwException) {
					throw new UcAdfInvalidValueException("${response.getStatus()} $errMsg")
				}
				
				UcdGenericProcessRequest ucdGenericProcessRequest = new UcdGenericProcessRequest()
				ucdGenericProcessRequest.setResult(UcdProcessRequestResponseResultEnum.FAILEDTOSTART)
				ucdGenericProcessRequests.add(ucdGenericProcessRequest)
			}
		} else {
			// Run a generic process for each resource.
			for (resource in resources) {
				requestMap = [
					processId: ucdGenericProcess.getId(),
					resource: resource ?: ucdGenericProcess.getDefaultResourceId(),
					properties: processPropertiesMap
				]
				
				JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

				UcdGenericProcessRequest ucdGenericProcessRequest
				
				Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
				if (response.getStatus() == 200) {
					ucdGenericProcessRequest = response.readEntity(UcdGenericProcessRequest.class)
					ucdGenericProcessRequests.add(ucdGenericProcessRequest)
				} else {
					String errMsg = response.readEntity(String.class)
					logVerbose(errMsg)
					if (throwException) {
						throw new UcAdfInvalidValueException("${response.getStatus()} $errMsg")
					}
				
					ucdGenericProcessRequest.setResult(UcdProcessRequestResponseResultEnum.FAILEDTOSTART)
					ucdGenericProcessRequests.add(ucdGenericProcessRequest)
				}
			}
		}

		if (waitForProcess && maxWaitSecs > 0) {
			// Wait for each process to finish.
			for (UcdGenericProcessRequest ucdGenericProcessRequest in ucdGenericProcessRequests) {
				if (ucdGenericProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.FAILEDTOSTART) {
					continue
				}
				
				String requestId = ucdGenericProcessRequest.getId()
				
				logVerbose("Waiting up to $maxWaitSecs seconds for generic process [$requestId] to complete.")
		
				Integer remainingSecs = maxWaitSecs
				while (true) {
					// Get information about the running generic process.
					UcdGenericProcessRequest evaluateGenericProcessRequest = actionsRunner.runAction([
						action: UcdGetGenericProcessRequest.getSimpleName(),
						actionInfo: false,
						actionVerbose: false,
						requestId: requestId
					])

					logDebug("result=${evaluateGenericProcessRequest.getResult()}")
					if (evaluateGenericProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.SUCCEEDED) {
						ucdGenericProcessRequest.setResult(evaluateGenericProcessRequest.getResult())
						ucdGenericProcessRequest.setState(evaluateGenericProcessRequest.getState())
						break
					} else if (evaluateGenericProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.FAULTED || evaluateGenericProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.FAILEDTOSTART) {
						if (throwException) {
							throw new UcAdfInvalidValueException("Generic process [$requestId] failed.")
						}
						
						ucdGenericProcessRequest.setResult(evaluateGenericProcessRequest.getResult())
						ucdGenericProcessRequest.setState(evaluateGenericProcessRequest.getState())
						break
					}
		
					remainingSecs -= waitIntervalSecs
					if (remainingSecs <= 0) {
						throw new UcAdfInvalidValueException("Generic process [$requestId] wait time exceeded [$maxWaitSecs] seconds.")
					}
					
					Thread.sleep(waitIntervalSecs * 1000)
				}
			}
		}
		
		return ucdGenericProcessRequests
	}
}
