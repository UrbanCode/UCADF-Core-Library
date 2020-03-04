/**
 * This action waits for a process request task to be waiting for a response.
 */
package org.urbancode.ucadf.core.action.ucd.processRequest

import org.urbancode.ucadf.core.action.ucd.applicationProcessRequest.UcdGetApplicationProcessRequest
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequestTask
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseStatusEnum
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTraceTypeEnum
import org.urbancode.ucadf.core.model.ucd.task.UcdTaskStatusEnum

class UcdWaitForProcessRequestTask extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String requestId

	/** The application process name. */	
	String processName

	/** The type of task in the trace. */
	UcdProcessRequestTraceTypeEnum type
	
	/** The task name. */
	String taskName

	/** (Optional) The desired status. Default is OPEN. */
	UcdTaskStatusEnum status = UcdTaskStatusEnum.OPEN
	
	/** The maximum number of seconds to wait. Default is 300. */
	Integer maxWaitSecs = 300
	
	/** The number of seconds to wait between each check. */
	Integer waitIntervalSecs = 30
	
	/** The flag that indicates fail if the application process request is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The process request task object.
	 */
	@Override
	public UcdComponentProcessRequestTask run() {
		// Validate the action properties.
		validatePropsExist()

		// Construct the task path to search for according to the task type.
		String taskPath
		if (type == UcdProcessRequestTraceTypeEnum.APPLICATIONMANUALTASK) {
			taskPath = ".*/Manual Task: $taskName"
		} else {
			taskPath = ".*/.*/.*$processName/.*$taskName"
		}
		
		logVerbose("Waiting for application request [$requestId] process [$processName] task [$taskName] to be available.")
			
        UcdComponentProcessRequestTask ucdProcessRequestTask
        
        Integer remainingSecs = maxWaitSecs
        while (true) {
			UcdApplicationProcessRequest ucdApplicationProcessRequest = actionsRunner.runAction([
				action: UcdGetApplicationProcessRequest.getSimpleName(),
				actionInfo: false,
				requestId: requestId,
				failIfNotFound: failIfNotFound
			])

	        // Get the process tasks information.
			ucdProcessRequestTask = actionsRunner.runAction([
				action: UcdFindProcessRequestTask.getSimpleName(),
				actionInfo: false,
				requestId: requestId,
				type: type,
				taskPath: taskPath,
				status: status,
				failIfNotFound: false
			])

            if (ucdProcessRequestTask) {
				logVerbose("Found request task with status of [${ucdProcessRequestTask.getStatus()}].")
				if (ucdProcessRequestTask.getStatus() == status) {
					break
				}
            }
            
            if (ucdApplicationProcessRequest.getState() == UcdProcessRequestResponseStatusEnum.CLOSED) {
                throw new UcdInvalidValueException("Application process [$requestId] ended before task information was available.")
            }

            if (remainingSecs < 1) {
                throw new UcdInvalidValueException("Timed out waiting for process task.")
            }
            
            logVerbose("Waiting a maximum of [$remainingSecs] more seconds for process task [$taskPath]. Next check in [$waitIntervalSecs] seconds.")
            Thread.sleep(waitIntervalSecs * 1000)
            remainingSecs -= waitIntervalSecs
        }
        
        return ucdProcessRequestTask
    }
}
