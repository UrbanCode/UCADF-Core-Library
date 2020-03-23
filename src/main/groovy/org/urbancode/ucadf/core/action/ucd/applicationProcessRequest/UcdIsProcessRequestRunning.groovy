/**
 * This action determines if a process request is running.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.action.ucd.componentProcessRequest.UcdGetComponentProcessRequest
import org.urbancode.ucadf.core.action.ucd.genericProcessRequest.UcdGetGenericProcessRequest
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcessRequest.UcdGenericProcessRequest
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseStatusEnum

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
		
		Boolean foundProcess = false
		Boolean isRunning = false
		
		// Determine if request ID is associated with a running application process.
		UcdApplicationProcessRequest appProcessRequest = actionsRunner.runAction([
			action: UcdGetApplicationProcessRequest.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			requestId: requestId,
			failIfNotFound: false
		])
		
		if (appProcessRequest) {
			foundProcess = true

			// Determine if the application process request is running.
			if (appProcessRequest.getState() && appProcessRequest.getState() != UcdProcessRequestResponseStatusEnum.CLOSED) {
				logVerbose("Application process request ID [$requestId] is running.")
				isRunning = true
			}
		} else {
			// Determine if request ID is associated with a running component process.
			UcdComponentProcessRequest compProcessRequest = actionsRunner.runAction([
				action: UcdGetComponentProcessRequest.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				requestId: requestId,
				failIfNotFound: false
			])

			if (compProcessRequest) {
				foundProcess = true
			
				if (compProcessRequest.getState() && compProcessRequest.getState() != UcdProcessRequestResponseStatusEnum.CLOSED) {
					logVerbose("Component process request ID [$requestId] is running.")
					isRunning = true
				}
			} else {
				// Determine if request ID is associated with a running generic process.
				UcdGenericProcessRequest genericProcessRequest = actionsRunner.runAction([
					action: UcdGetGenericProcessRequest.getSimpleName(),
					actionInfo: false,
					actionVerbose: false,
					requestId: requestId,
					failIfNotFound: false
				])
				
				if (genericProcessRequest) {
					foundProcess = true

					if (genericProcessRequest.getState() && genericProcessRequest.getState() != UcdProcessRequestResponseStatusEnum.CLOSED) {
						logVerbose("Generic process request ID [$requestId] is running.")
						isRunning = true
					}
				}
			}
		}

		if (!foundProcess && failIfNotFound) {
			throw new UcAdfInvalidValueException("Process request [$requestId] not found.")
		}		
		
		return isRunning
	}
}
