/**
 * This action runs a component process.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcessRequest

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.action.ucd.componentProcess.UcdGetComponentProcess
import org.urbancode.ucadf.core.action.ucd.environment.UcdGetEnvironment
import org.urbancode.ucadf.core.action.ucd.resource.UcdGetResource
import org.urbancode.ucadf.core.action.ucd.version.UcdGetVersion
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequestStatus
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseStatusEnum
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdRunComponentProcess extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/** The component name or ID. */
	String component
	
	/** The component process name or ID. */
	String process

	/** The resource name or ID. */
	String resource
		
	/** The version name or ID. */
	String version

	/** (Optional) The list of component process request properties. */
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
	 * @return The component process request object.
	 */
	@Override
	public UcdComponentProcessRequestStatus run() {
		// Validate the action properties.
		validatePropsExist()
		
		UcdComponentProcessRequestStatus ucdComponentProcessRequestStatus = new UcdComponentProcessRequestStatus()
		
		logVerbose("Running component [$component] process [$process] on resource [$resource] with version [$version].")

		// Get the environment information.
		String environmentId
		if (UcdObject.isUUID(environment)) {
			environmentId = environment
		} else {
			UcdEnvironment ucdEnvironment = actionsRunner.runAction([
				action: UcdGetEnvironment.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				application: application,
				environment: environment,
				failIfNotFound: true
			])
			
			environmentId = ucdEnvironment.getId()
		}
		
		// Get the component information.
		String componentId
		if (UcdObject.isUUID(component)) {
			componentId = component
		} else {
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				failIfNotFound: true
			])
			
			componentId = ucdComponent.getId()
		}
		
		// Get the component process information.
		String processId
		if (UcdObject.isUUID(process)) {
			processId = process
		} else {
			UcdComponentProcess ucdComponentProcess = actionsRunner.runAction([
				action: UcdGetComponentProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				process: process,
				failIfNotFound: true
			])
			
			processId = ucdComponentProcess.getId()
		}
		
		// Get the resource information.
		String resourceId
		if (UcdObject.isUUID(resource)) {
			resourceId = resource
		} else {
			UcdResource ucdResource = actionsRunner.runAction([
				action: UcdGetResource.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				resource: resource,
				failIfNotFound: true
			])
			
			resourceId = ucdResource.getId()
		}
		
		// Get the version information.
		String versionId
		if (UcdObject.isUUID(version)) {
			versionId = version
		} else {
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetVersion.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: componentId,
				version: version,
				failIfNotFound: true
			])
			
			versionId = ucdVersion.getId()
		}
		
		// Build a custom post body that includes only the required fields.
		Map processPropertiesMap = [:]
		processPropertiesMap.put("versionId", versionId)
		for (ucdProperty in ucdProperties) {
			processPropertiesMap.put(ucdProperty.getName(), ucdProperty.getValue())
		}

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component/{componentId}/runProcess")
			.resolveTemplate("componentId", componentId)
		logDebug("target=$target")
		
		// Construct the request.
		Map requestMap = [
			environmentId: environmentId,
			resourceId: resourceId,
			componentProcessId: processId,
			versionId: versionId,
			properties: processPropertiesMap
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
		logDebug("jsonBuilder=${jsonBuilder.toString()}")

		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))

		if (response.getStatus() == 200) {
			ucdComponentProcessRequestStatus = response.readEntity(UcdComponentProcessRequestStatus.class)
			if (waitForProcess && maxWaitSecs > 0) {
				String requestId = ucdComponentProcessRequestStatus.getRequestId()
				
				logVerbose("Waiting up to $maxWaitSecs seconds for component process [$requestId] to complete.")
		
				Integer remainingSecs = maxWaitSecs
				while (true) {
					// Get information about the running component process.
					UcdComponentProcessRequest evaluateComponentProcessRequest = actionsRunner.runAction([
						action: UcdGetComponentProcessRequest.getSimpleName(),
						actionInfo: false,
						actionVerbose: false,
						requestId: requestId
					])

					if (evaluateComponentProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.SUCCEEDED) {
						ucdComponentProcessRequestStatus.setResult(evaluateComponentProcessRequest.getResult())
						ucdComponentProcessRequestStatus.setState(evaluateComponentProcessRequest.getState())
						break
					} else if (evaluateComponentProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.FAULTED || evaluateComponentProcessRequest.getResult() == UcdProcessRequestResponseResultEnum.FAILEDTOSTART) {
						if (throwException) {
							throw new UcAdfInvalidValueException("Component process [$requestId] failed.")
						}
						
						ucdComponentProcessRequestStatus.setResult(evaluateComponentProcessRequest.getResult())
						ucdComponentProcessRequestStatus.setState(evaluateComponentProcessRequest.getState())
						break
					}
		
					remainingSecs -= waitIntervalSecs
					if (remainingSecs <= 0) {
						throw new UcAdfInvalidValueException("Component process [$requestId] wait time exceeded [$maxWaitSecs] seconds.")
					}
					
					Thread.sleep(waitIntervalSecs * 1000)
				}
			}
		} else {
			logError(response.readEntity(String.class))
			logError("Status: ${response.getStatus()} Can't run component process. $target.")
			ucdComponentProcessRequestStatus.setResult(UcdProcessRequestResponseResultEnum.FAILEDTOSTART)
			ucdComponentProcessRequestStatus.setState(UcdProcessRequestResponseStatusEnum.FAULTED)
		}
		
		// Throw exception if requested instead of letting output properties status return to plugin step.
		if (!UcdProcessRequestResponseResultEnum.SUCCEEDED.equals(ucdComponentProcessRequestStatus.getResult()) && throwException) {
			throw new UcAdfInvalidValueException("Component process failed.")
		}

		return ucdComponentProcessRequestStatus
	}
}
