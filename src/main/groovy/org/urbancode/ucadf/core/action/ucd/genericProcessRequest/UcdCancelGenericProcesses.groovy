/**
 * This action cancels generic process requests.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcessRequest

import org.urbancode.ucadf.core.action.ucd.workflow.UcdCancelWorkflow
import org.urbancode.ucadf.core.action.ucd.workflow.UcdGetWorkflowActivity
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.workflow.UcdWorkflowApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucd.workflow.UcdWorkflowGenericProcessRequest
import org.urbancode.ucadf.core.model.ucd.workflow.UcdWorkflowProcessRequest

class UcdCancelGenericProcesses extends UcAdfAction {
	// Action properties.
	/** The regular expression used to find matching process names to cancel. Default is .* (all). */
	String matchProcess = ".*"
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

        logInfo("Cancelling all generic processes matching [$matchProcess].")

		List<UcdWorkflowProcessRequest> ucdWorkflowProcessRequests = actionsRunner.runAction([
			action: UcdGetWorkflowActivity.getSimpleName(),
			actionInfo: false
		])

        for (UcdWorkflowProcessRequest ucdWorkflowProcessRequest in ucdWorkflowProcessRequests) {
            if (!(ucdWorkflowProcessRequest instanceof UcdWorkflowGenericProcessRequest)) {
                continue
            }

			// Recast the workflow request.
			UcdWorkflowGenericProcessRequest genericWorkflowRequest = ucdWorkflowProcessRequest

            if (genericWorkflowRequest.getProcess().getName() ==~ matchProcess) {
				actionsRunner.runAction([
					action: UcdCancelWorkflow.getSimpleName(),
					actionInfo: false,
					workflow: genericWorkflowRequest.getWorkflowTraceId()
				])
		
				logInfo("Cancelled generic process [${genericWorkflowRequest.getProcess().getName()}].")
            }
        }
		
        logInfo("All running generic processes matching [$matchProcess] cancelled.")
	}
}
