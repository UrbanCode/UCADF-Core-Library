/**
 * This action runs an application process.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import java.text.SimpleDateFormat

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestOutPropEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestResponseExpectEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestStatus
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestStatusEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestVersion
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdRunApplicationProcess extends UcAdfAction {
	// Constants.
	private final static String SCHEDULED_DATE_FORMAT = "yyyy-MM-dd HH:mm"

	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/** The process name or ID. */
	String process
	
	/** (Optional) The application process request description. */
	String description = ""
	
	/** (Optional) The list of application process request properties. */
	@JsonProperty("properties")
	List<UcdProperty> ucdProperties = []
	
	/** (Optional) The application process request snapshot. */
	String snapshot = ""
	
	/** (Optional) The application process request versions. */
	List<UcdApplicationProcessRequestVersion> versions = []
	
	/** If true then deploy only changed versions. Default is true. */
	Boolean onlyChanged = true
	
	/** If true then wait for the process to complete. Default is true. */
	Boolean waitForProcess = true
	
	/** The number of seconds to wait between each check. */
	Integer waitIntervalSecs = 3
	
	/** The maximum number of seconds to wait. Default is 600. */
	Integer maxWaitSecs = 600
	
	/** If true then then an exception will be thrown if the process fails. Default is true. */
	Boolean throwException = true
	
	/** (Optional) The UCD instance alias that makes details links use the alias instead of the default UCD server name. */
	String detailsLinkAlias = ""

	/** (Optional) The expected status return values. */
	UcdApplicationProcessRequestResponseExpectEnum expect

	/** (Optional) The scheduled start date (GMT). */	
	String scheduledStartDate
	
	/** (Optional) The file to which the status will be written. */
	File processResultsFile

	/**
	 * Runs the action.	
	 * @return The application process request status object.
	 */
	@Override
	public UcdApplicationProcessRequestStatus run() {
		// Validate the action properties.
		validatePropsExistExclude(
			[
				"scheduledStartDate",
				"processResultsFile",
				"expect"
			]
		)

		Response response 
		String requestId = ""
		String detailsLink = ""
		UcdApplicationProcessRequestStatus ucdApplicationProcessRequestStatus = new UcdApplicationProcessRequestStatus()
		
		// Initialize the application request status.
		ucdApplicationProcessRequestStatus.setApplicationProcessStatus(UcdApplicationProcessRequestStatusEnum.SUCCESS)

		Map processPropertiesMap = [:]
		for (ucdProperty in ucdProperties) {
			processPropertiesMap.put(ucdProperty.getName(), ucdProperty.getValue())
		}
		
		Map requestMap = [
			application: application, 
			description: description, 
			environment: environment, 
			applicationProcess: process, 
			properties: processPropertiesMap, 
			onlyChanged: onlyChanged
		]
		
		if (snapshot) {
			requestMap["snapshot"] = snapshot
		} else if (versions && versions.size() > 0) {
			requestMap["versions"] = versions
		}
		
		if (scheduledStartDate) {
			requestMap["date"] = scheduledStartDate
		}
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

		logDebug("Application process request:\n${jsonBuilder.toPrettyString()}")

		// Run the application process.
		logVerbose("Running application [$application] environment [$environment] process [$process] on ${ucdSession.getUcdUrl()}.")
		
		WebTarget requestTarget = ucdSession.getUcdWebTarget().path("/cli/applicationProcessRequest/request")
		logDebug("requestTarget=$requestTarget")

		// Submit the request.
		response = requestTarget.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			// Parse the response.
			ucdApplicationProcessRequestStatus = response.readEntity(UcdApplicationProcessRequestStatus.class)
			requestId = ucdApplicationProcessRequestStatus.getRequestId()
			
			logVerbose("Application process request ID [$requestId].")
			
			outProps.put(
				UcdApplicationProcessRequestOutPropEnum.REQUESTID.getPropertyName(), 
				requestId
			)
			
			// Get the process details link. Default to using the instance URL and optionally, replace the host name portion of the URL with the alias.
			String useDetailsLinkAlias = ucdSession.getUcdUrl()
			if (detailsLinkAlias) {
				useDetailsLinkAlias = useDetailsLinkAlias.replaceAll(/^(http[s]?:\/\/)(.*?)(\..*)/, '$1' + detailsLinkAlias + '$3')
			}

			detailsLink = "$useDetailsLinkAlias/#applicationProcessRequest/$requestId".toString()
			logVerbose("detailsLink=$detailsLink")
			
			outProps.put(
				UcdApplicationProcessRequestOutPropEnum.DETAILSLINK.getPropertyName(), 
				detailsLink
			)

			// Wait for the process to complete.
			if (waitForProcess && maxWaitSecs > 0) {
				ucdApplicationProcessRequestStatus = actionsRunner.runAction([
					action: UcdWaitForApplicationProcessRequest.getSimpleName(),
					actionInfo: false,
					requestId: requestId,
					waitIntervalSecs: waitIntervalSecs,
					maxWaitSecs: maxWaitSecs,
					throwException: throwException
				])
			} else {
				ucdApplicationProcessRequestStatus.setApplicationProcessStatus(UcdApplicationProcessRequestStatusEnum.SUCCESS)
			}
		} else {
			logError(response.readEntity(String.class))
			logError("Status: ${response.getStatus()} Can't run application process. $requestTarget.")
			ucdApplicationProcessRequestStatus.setApplicationProcessStatus(UcdApplicationProcessRequestStatusEnum.FAILURE)
		}

		// The status it returned to a plugin call via the output properties.
		outProps.put(
			UcdApplicationProcessRequestOutPropEnum.PROCESSSTATUS.getPropertyName(), 
			ucdApplicationProcessRequestStatus.getApplicationProcessStatus().getValue()
		)

		outProps.put(
			UcdApplicationProcessRequestOutPropEnum.RESPONSERESULT.getPropertyName(), 
			ucdApplicationProcessRequestStatus.getResult().getValue()
		)
		
		outProps.put(
			UcdApplicationProcessRequestOutPropEnum.RESPONSESTATUS.getPropertyName(), 
			ucdApplicationProcessRequestStatus.getStatus().getValue()
		)
		
		logVerbose("outProps=$outProps")

		// Write the results properties to a file.
		if (processResultsFile != null) {
			logVerbose("Writing process request information to [" + processResultsFile + "] file.")
			PrintWriter writer = new PrintWriter(processResultsFile, "UTF-8")
			outProps.each { outPropName, outPropValue ->
				String line = outPropName + "=" + outPropValue
				System.out.println(line)
				writer.println(line)
			}
			writer.close()
		}

		// Validate the expected response was returned.
		if (expect) {
			expect.validate(
				response.getStatus(),
				ucdApplicationProcessRequestStatus.getResult()
			)
		} else {
			// Throw exception if requested instead of letting output properties status return to plugin step.
			if (UcdApplicationProcessRequestStatusEnum.SUCCESS != ucdApplicationProcessRequestStatus.getApplicationProcessStatus() && throwException) {
				throw new UcdInvalidValueException("Application process failed.")
			}
		}
		
		return ucdApplicationProcessRequestStatus
	}
}
