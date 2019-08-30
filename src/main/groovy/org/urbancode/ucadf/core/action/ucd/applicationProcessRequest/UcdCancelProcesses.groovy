/**
 * This action cancels application process requests.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.action.ucd.workflow.UcdCancelWorkflow
import org.urbancode.ucadf.core.action.ucd.workflow.UcdGetWorkflowActivity
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.workflow.UcdWorkflowApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucd.workflow.UcdWorkflowProcessRequest

class UcdCancelProcesses extends UcAdfAction {
	// Action properties.
	/** The regular expression used to find matching process names to cancel. Default is .* (all). */
	String match = ".*"
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

        logInfo("Cancelling all running application processes matching [$match].")

		List<UcdWorkflowProcessRequest> ucdWorkflowProcessRequests = actionsRunner.runAction([
			action: UcdGetWorkflowActivity.getSimpleName(),
			actionInfo: false
		])

        for (UcdWorkflowProcessRequest ucdWorkflowProcessRequest in ucdWorkflowProcessRequests) {
            if (!(ucdWorkflowProcessRequest instanceof UcdWorkflowApplicationProcessRequest)) {
                continue
            }

			// Recast the workflow request.
			UcdWorkflowApplicationProcessRequest appWorkflowRequest = ucdWorkflowProcessRequest

            if (appWorkflowRequest.getApplicationProcess().getName() ==~ match) {
				actionsRunner.runAction([
					action: UcdCancelWorkflow.getSimpleName(),
					actionInfo: false,
					workflow: appWorkflowRequest.getTraceId()
				])
		
				logInfo("Cancelled application process [${appWorkflowRequest.getApplicationProcess().getName()}] [${appWorkflowRequest.getApplicationProcess().getId()}].")
            }
        }
		
        logInfo("All running application processes matching [$match] cancelled.")
	}
}
