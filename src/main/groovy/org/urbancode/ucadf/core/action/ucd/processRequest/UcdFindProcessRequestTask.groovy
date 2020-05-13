/**
 * This action finds a process task.
 */
package org.urbancode.ucadf.core.action.ucd.processRequest

import org.urbancode.ucadf.core.action.ucd.applicationProcessRequest.UcdGetApplicationProcessRequest
import org.urbancode.ucadf.core.action.ucd.componentProcessRequest.UcdGetComponentProcessRequest
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTask
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTrace
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTraceTypeEnum
import org.urbancode.ucadf.core.model.ucd.task.UcdTaskStatusEnum

class UcdFindProcessRequestTask extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String requestId

	/** The type of task in the trace. */
	UcdProcessRequestTraceTypeEnum type
	
	/** The path to the task in the trace. */
	String taskPath
	
	/** (Optional) Find a task with this status. */
	UcdTaskStatusEnum status
	
	/** The flag that indicates fail if the process request task is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The process request task object.
	 */
	@Override
	public UcdProcessRequestTask run() {
		// Validate the action properties.
		validatePropsExistExclude(
			[
				"status"
			]
		)

		logVerbose("Finding process [$requestId] task type [$type] task path [$taskPath] with status [$status].")

		// Only certin types are supported now.
		if (type != UcdProcessRequestTraceTypeEnum.APPLICATIONMANUALTASK && type != UcdProcessRequestTraceTypeEnum.COMPONENTMANUALTASK) {
			throw new UcAdfInvalidValueException("Finding tasks for type [$type] not currently supported.")
		}		
		
		// Get the application process request.
		UcdApplicationProcessRequest ucdApplicationProcessRequest = actionsRunner.runAction([
			action: UcdGetApplicationProcessRequest.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			requestId: requestId,
			failIfNotFound: failIfNotFound
		])

		// Find the process request task.
		UcdProcessRequestTask processRequestTask = findProcessTaskInTrace(
			"",
			ucdApplicationProcessRequest.getRootTrace(),
			taskPath,
			status
		)

		if (!processRequestTask && failIfNotFound) {
			throw new UcAdfInvalidValueException("Process [$requestId] task type [$type] task path [$taskPath] with status [$status] not found.")
		}
		
		return processRequestTask
    }	
	
    // In an application process request trace, recursively find a task of a given name in a process of a given name.
    public UcdProcessRequestTask findProcessTaskInTrace(
		final String path, 
		final UcdProcessRequestTrace ucdProcessRequestTopTrace, 
		final String taskPath,
		final UcdTaskStatusEnum status = null) {

        UcdProcessRequestTask ucdProcessRequestTask

		UcdProcessRequestTrace ucdProcessRequestTrace = ucdProcessRequestTopTrace
		String derivedPath = path

		if (ucdProcessRequestTrace) {
	        if (ucdProcessRequestTrace.getDisplayName()) {
	            derivedPath += "/${ucdProcessRequestTrace.getDisplayName()}"
	        } else {
	            derivedPath += "/${ucdProcessRequestTrace.getName()}"
	        } 
			
			logDebug("type=[$type] taskPath=[$taskPath] trace type= [${ucdProcessRequestTrace.getType()}] derivedPath [$derivedPath] name [${ucdProcessRequestTrace.getName()}].")
			
	        if (type.getValue() == ucdProcessRequestTrace.getType() && derivedPath ==~ /$taskPath/) {
				 if (!status || (ucdProcessRequestTrace.getTask() && ucdProcessRequestTrace.getTask().getStatus() == status)) {
				 	ucdProcessRequestTask = ucdProcessRequestTrace.getTask()
				 }
	        }
			
			if (!ucdProcessRequestTask) {
	            // If the trace entry is a component process request then get the trace for that request.
	            if (ucdProcessRequestTrace.getComponentProcessRequestId()) {
	                if (ucdProcessRequestTrace.getComponent()) {
	                    derivedPath += "/${ucdProcessRequestTrace.getComponent().getName()}"
	                }
	                derivedPath += "/${ucdProcessRequestTrace.getComponentProcess().getName()}"
					
					// Get the component process request.
					ucdProcessRequestTrace = actionsRunner.runAction([
						action: UcdGetComponentProcessRequest.getSimpleName(),
						actionInfo: false,
						actionVerbose: false,
						requestId: ucdProcessRequestTrace.getComponentProcessRequestId()
					])
	            }
	            
	            // Recursively process the children.
	            if (ucdProcessRequestTrace.getChildren()) {
	                for (childTrace in ucdProcessRequestTrace.getChildren()) {
	                    ucdProcessRequestTask = findProcessTaskInTrace(
							derivedPath, 
							childTrace, 
							taskPath,
							status
						)
						
	                    if (ucdProcessRequestTask) {
	                        break
	                    }
	                }
	            }
	        }
		}
		
        return ucdProcessRequestTask
    }
}
