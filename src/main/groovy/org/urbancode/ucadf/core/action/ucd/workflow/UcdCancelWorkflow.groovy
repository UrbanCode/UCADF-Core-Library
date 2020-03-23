/**
 * This action cancels a workflow.
 */
package org.urbancode.ucadf.core.action.ucd.workflow

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdCancelWorkflow extends UcAdfAction {
	// Action properties.
	/** The workflow ID. */
	String workflow
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

        logVerbose("Cancelling workflow [$workflow].")
		
        WebTarget target = ucdSession.getUcdWebTarget().path("/rest/workflow/{workflowId}/cancel")
			.resolveTemplate("workflowId", workflow)
        logDebug("target=$target")
		
        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(""))
        if (response.getStatus() == 200) {
            logVerbose("Workflow [$workflow] cancelled.")
        } else {
            throw new UcAdfInvalidValueException(response)
        }
	}
}
