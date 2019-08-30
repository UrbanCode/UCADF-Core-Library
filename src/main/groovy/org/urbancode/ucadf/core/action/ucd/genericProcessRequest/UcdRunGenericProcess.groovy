/**
 * This action runs a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcessRequest

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.genericProcess.UcdGetGenericProcess
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.genericProcessRequest.UcdGenericProcessRequest
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdRunGenericProcess extends UcAdfAction {
	// Action properties.
	/** The generic process name or ID. */
	String process
	
	/** (Optional) The resource path or ID. */
	String resource = ""
	
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
	 * @return The generic process request object.
	 */
	@Override
	public UcdGenericProcessRequest run() {
		// Validate the action properties.
		validatePropsExist()
		
		UcdGenericProcessRequest ucdGenericProcessRequest
		
		logInfo("Running generic process [$process]")

		// Get the generic process information.
		UcdGenericProcess ucdGenericProcess = actionsRunner.runAction([
			action: UcdGetGenericProcess.getSimpleName(),
			actionInfo: false,
			process: process,
			failIfNotFound: true
		])

		// Build a custom post body that includes only the required fields.
		String requestId

		Map processPropertiesMap = [:]
		for (ucdProperty in ucdProperties) {
			processPropertiesMap.put(ucdProperty.getName(), ucdProperty.getValue())
		}

		// Construct the request.
		Map requestMap = [
			processId: ucdGenericProcess.getId(),
			resource: resource ?: ucdGenericProcess.getDefaultResourceId(),
			properties: processPropertiesMap
		]

		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/request")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			ucdGenericProcessRequest = response.readEntity(UcdGenericProcessRequest.class)
			requestId = ucdGenericProcessRequest.getId()
			logInfo("Generic process request ID [$requestId].")
			
			if (waitForProcess && maxWaitSecs > 0) {
				logInfo("Waiting up to $maxWaitSecs seconds for generic process [$requestId] to complete.")
		
				Integer remainingSecs = maxWaitSecs
				while (true) {
					// Get information about the running generic process.
					ucdGenericProcessRequest = actionsRunner.runAction([
						action: UcdGetGenericProcessRequest.getSimpleName(),
						actionInfo: false,
						requestId: requestId
					])

					logDebug("result=${ucdGenericProcessRequest.getResult()}")
					if (ucdGenericProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.SUCCEEDED) {
						break
					} else if (ucdGenericProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.FAULTED || ucdGenericProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.FAILEDTOSTART) {
						if (throwException) {
							throw new UcdInvalidValueException("Generic process [$requestId] failed.")
						}
						break
					}
		
					remainingSecs -= waitIntervalSecs
					if (remainingSecs <= 0) {
						throw new UcdInvalidValueException("Generic process [$requestId] wait time exceeded [$maxWaitSecs] seconds.")
					}
		
					Thread.sleep(waitIntervalSecs * 1000)
				}
			}
		} else {
			String errMsg = response.readEntity(String.class)
			logInfo(errMsg)
			if (throwException) {
				throw new UcdInvalidValueException(errMsg)
			}
			
			ucdGenericProcessRequest = new UcdGenericProcessRequest()
			ucdGenericProcessRequest.setResult(UcdProcessRequestResponseResultEnum.FAILEDTOSTART)
		}
		
		return ucdGenericProcessRequest
	}
}
