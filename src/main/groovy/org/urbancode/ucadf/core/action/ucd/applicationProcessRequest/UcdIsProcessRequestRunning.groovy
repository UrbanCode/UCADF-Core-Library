/**
 * This action determines if a process request is running.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.action.ucd.componentProcessRequest.UcdGetComponentProcessRequest
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequestResponseStatusEnum
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdIsProcessRequestRunning extends UcAdfAction {
	// Action properties.
	/** The process request ID. */
	String requestId
	
	/** The flag that indicates fail if the process request is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return True if the process request is running.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()
		
		Boolean isRunning = false
		
		// Determine if request ID is associated with a running application process.
		UcdApplicationProcessRequest appProcessRequest = actionsRunner.runAction([
			action: UcdGetApplicationProcessRequest.getSimpleName(),
			requestId: requestId
		])
		
		if (appProcessRequest) {
			// Determin if the application process request is running.
			if (appProcessRequest.getState() && appProcessRequest.getState() != UcdApplicationProcessRequestResponseStatusEnum.CLOSED) {
				logVerbose("Application process request ID [$requestId] is running.")
				isRunning = true
			}
		} else {
			// Determine if request ID is associated with a running component process.
			UcdComponentProcessRequest compProcessRequest = actionsRunner.runAction([
				action: UcdGetComponentProcessRequest.getSimpleName(),
				actionInfo: false,
				requestId: requestId,
				failIfNotFound: failIfNotFound
			])
			
			if (compProcessRequest) {
				if (compProcessRequest.getState() && compProcessRequest.getState() != UcdApplicationProcessRequestResponseStatusEnum.CLOSED) {
					logVerbose("Component process request ID [$requestId] is running.")
					isRunning = true
				}
			} else {
				throw new UcdInvalidValueException("Application or component process ]$requestId] not found.")
			}
		}
		
		return isRunning
	}
}
