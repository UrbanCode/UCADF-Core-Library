/**
 * This action gets the list of workflow process requests.
 */
package org.urbancode.ucadf.core.action.ucd.workflow

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.workflow.UcdWorkflowProcessRequest

class UcdGetWorkflowActivity extends UcAdfAction {
	// Action properties.
	String parentRequestId = ""
	
	/**
	 * Runs the action.	
	 * @return The list of workflow process requests.
	 */
	@Override
	public List<UcdWorkflowProcessRequest> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdWorkflowProcessRequest> ucdProcessRequests = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/workflow/currentActivity")
		
		if (parentRequestId) {
			target = target.queryParam("parentRequestId", parentRequestId)
		}
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdProcessRequests = response.readEntity(new GenericType<List<UcdWorkflowProcessRequest>>(){})
		} else {
			throw new UcAdfInvalidValueException(response)
		}
		
		return ucdProcessRequests
	}
}
