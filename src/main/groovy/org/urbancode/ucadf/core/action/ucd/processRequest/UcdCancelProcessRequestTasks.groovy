/**
 * This action cancels application process requests.
 */
package org.urbancode.ucadf.core.action.ucd.processRequest

import org.urbancode.ucadf.core.action.ucd.task.UcdProvideTaskResponse
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTask
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTraceTypeEnum
import org.urbancode.ucadf.core.model.ucd.task.UcdTaskResponseEnum
import org.urbancode.ucadf.core.model.ucd.task.UcdTaskStatusEnum

class UcdCancelProcessRequestTasks extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String requestId
	
	/** The process name. */
	String processName

	/** The type of task in the trace. */
	UcdProcessRequestTraceTypeEnum type
	
	/** The task name. */
	String taskName
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Construct the task path to search for according to the task type.
		String taskPath
		if (type == UcdProcessRequestTraceTypeEnum.APPLICATIONMANUALTASK) {
			taskPath = ".*/Manual Task: $taskName"
		} else {
			taskPath = ".*/.*/.*$processName/.*$taskName"
		}
		
		logInfo("Cancel application request [$requestId] process [$processName] task [$taskName].")

        // Get the process tasks information.
		UcdProcessRequestTask ucdProcessRequestTask = actionsRunner.runAction([
			action: UcdFindProcessRequestTask.getSimpleName(),
			requestId: requestId,
			type: type,
            taskPath: taskPath,
			failIfNotFound: false
		])

        // If the task is open then fail it.
        if (ucdProcessRequestTask && UcdTaskStatusEnum.OPEN == ucdProcessRequestTask.getStatus()) {
            logInfo("Rejecting task [$taskName] [${ucdProcessRequestTask.getId()}].")

			actionsRunner.runAction([
				action: UcdProvideTaskResponse.getSimpleName(),
				taskId: ucdProcessRequestTask.getId(),
				passFail: UcdTaskResponseEnum.FAILED, 
				comment: "Terminating Task", 
                properties: ucdProcessRequestTask.getTaskCustomPropertyValues()
			])
        }
    }
}
