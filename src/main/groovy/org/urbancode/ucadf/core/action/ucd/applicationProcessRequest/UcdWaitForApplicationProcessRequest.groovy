/**
 * This action waits for an application process request to complete.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestOutPropEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestResponseExpectEnum
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestStatus
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestStatusEnum
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum

class UcdWaitForApplicationProcessRequest extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String requestId
	
	/** The number of seconds to wait between each check. */
	Integer waitIntervalSecs = 3
	
	/** The maximum number of seconds to wait. Default is 600. */
	Integer maxWaitSecs = 600
	
	/** If true then then an exception will be thrown if the process fails. Default is true. */
	Boolean throwException = true
	
	/** (Optional) The file to which the status will be written. */
	File processResultsFile
	
	/** (Optional) The expected status return values. */
	UcdApplicationProcessRequestResponseExpectEnum expect

	/**
	 * Runs the action.	
	 * @return The application process request status object.
	 */
	@Override
	public UcdApplicationProcessRequestStatus run() {
		// Validate the action properties.
		validatePropsExistExclude(
			[
				"processResultsFile",
				"expect"
			]
		)

        logInfo("Waiting up to [$maxWaitSecs] seconds for application process [$requestId] to complete.")

		Response response
        UcdApplicationProcessRequestStatus ucdApplicationProcessRequestStatus = new UcdApplicationProcessRequestStatus()
		
		Integer remainingSecs = maxWaitSecs
		while (true) {
			// Get the process status.
			WebTarget statusTarget = ucdSession.getUcdWebTarget().path("/cli/applicationProcessRequest/requestStatus")
				.queryParam("request", requestId)
			logDebug("statusTarget=$statusTarget")
			
			response = statusTarget.request().get()
			if (response.getStatus() != 200) {
				logError(response.readEntity(String.class))
				logError("Status: ${response.getStatus()} Can't get request status for request [$requestId]. $statusTarget")
				ucdApplicationProcessRequestStatus.setApplicationProcessStatus(UcdApplicationProcessRequestStatusEnum.FAILURE)
				break
			}

			// Parse the status response.
			ucdApplicationProcessRequestStatus = response.readEntity(UcdApplicationProcessRequestStatus.class)

			// The status response doesn't include the request ID so we have to add it.
			ucdApplicationProcessRequestStatus.setRequestId(requestId)

			logDebug("result=${ucdApplicationProcessRequestStatus.getResult()}")
			logDebug("status=${ucdApplicationProcessRequestStatus.getStatus()}")
			if (ucdApplicationProcessRequestStatus.getResult() == UcdProcessRequestResponseResultEnum.SUCCEEDED) {
				ucdApplicationProcessRequestStatus.setApplicationProcessStatus(UcdApplicationProcessRequestStatusEnum.SUCCESS)
				break
			}
			
			if (ucdApplicationProcessRequestStatus.getResult() == UcdProcessRequestResponseResultEnum.FAULTED
				|| ucdApplicationProcessRequestStatus.getResult() == UcdProcessRequestResponseResultEnum.FAILEDTOSTART
				|| ucdApplicationProcessRequestStatus.getResult() == UcdProcessRequestResponseResultEnum.CANCELED) {
				
				logError("Application process terminated with status [${ucdApplicationProcessRequestStatus.getResult()}].")
				ucdApplicationProcessRequestStatus.setApplicationProcessStatus(UcdApplicationProcessRequestStatusEnum.FAILURE)
				break
			}

			remainingSecs -= waitIntervalSecs
			if (remainingSecs <= 0) {
				logError("Application process wait time exceeded [$maxWaitSecs] seconds.")
				ucdApplicationProcessRequestStatus.setApplicationProcessStatus(UcdApplicationProcessRequestStatusEnum.FAILURE)
				break
			}

			Thread.sleep(waitIntervalSecs * 1000)
		}
				
        logInfo("Application process result [${ucdApplicationProcessRequestStatus.getResult().getValue()}] status [${ucdApplicationProcessRequestStatus.getStatus().getValue()}].")
        
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
		
		logInfo("outProps=$outProps")

		// Write the results properties to a file.
		if (processResultsFile != null) {
			logInfo("Writing process request information to [" + processResultsFile + "] file.")
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
