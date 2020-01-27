/**
 * This action gets a process request trace.
 */
package org.urbancode.ucadf.core.action.ucd.workflow

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.applicationProcessRequest.UcdGetApplicationProcessRequest
import org.urbancode.ucadf.core.action.ucd.componentProcessRequest.UcdGetComponentProcessRequest
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTrace
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

// TODO: This is not complete and does not currently return anything.
class UcdGetProcessRequestTrace extends UcAdfAction {
	// Action properties.
	/** The application process request ID. */
	String appProcessRequestId
	
	/** The component process request ID. */
	String compProcessRequestId
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Get an application process request trace.
		if (appProcessRequestId) {
			UcdApplicationProcessRequest ucdApplicationProcessRequest = actionsRunner.runAction([
				action: UcdGetApplicationProcessRequest.getSimpleName(),
				requestId: appProcessRequestId
			])

			// Recursively process the child traces.			
			getRequestTrace(ucdApplicationProcessRequest.getRootTrace())
		}

		// Get a component process request trace.		
		if (compProcessRequestId) {
			UcdComponentProcessRequest ucdComponentProcessRequest = actionsRunner.runAction([
				action: UcdGetComponentProcessRequest.getSimpleName(),
				requestId: compProcessRequestId
			])
			
			// Recursively process the child traces.
			if (ucdComponentProcessRequest.getChildren()) {
				for (childTrace in ucdComponentProcessRequest.getChildren()) {
					getRequestTrace(childTrace)
				}
			}	
		}
	}
	
	// Get the process request trace information.
	public getRequestTrace(UcdProcessRequestTrace trace) {
		logDebug("[${trace.getDisplayName()}] [${trace.getId()}] workflowTraceId [${trace.getWorkflowTraceId()}] componentProcessRequestId [${trace.getComponentProcessRequestId()}]")
		
		// Get the workflow step logs.
		if (trace.getWorkflowTraceId()) {
			if (trace.getExtraLogs() != null) {
                try {
    				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/logView/trace/{workflowTraceId}/{stepId}/stdOut.txt")
						.resolveTemplate("workflowTraceId", trace.getWorkflowTraceId())
						.resolveTemplate("stepId", trace.getId())
						.queryParam("fileDownload", "true")
    				logDebug("target=$target")
					
    				trace.setLogText(target.request().get(String.class))
                } catch(Exception e) {
                    logVerbose("Ignoring unable to get log information ${e.getMessage()}")
                    // Ignoring some kind of problem with this where it throws 500 errors.
                }
			}

			// Get the workflow step properties			
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/workflow/{workflowTraceId}/{stepId}/properties")
				.resolveTemplate("workflowTraceId", trace.getWorkflowTraceId())
				.resolveTemplate("stepId", trace.getId())
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				Map responseMap = response.readEntity(new GenericType<Map<String, List<UcdProperty>>>(){})
				trace.setStepProperties(responseMap.get("properties"))
				trace.setStepOutputProps(responseMap.get("outputProps"))
			} else {
				throw new UcdInvalidValueException(response)
			}
		}

		// Get the component process request trace.
		if (trace.getComponentProcessRequestId()) {
			UcdComponentProcessRequest ucdComponentProcessRequest = actionsRunner.runAction([
				action: UcdGetComponentProcessRequest.getSimpleName(),
				requestId: trace.getComponentProcessRequestId()
			])
			
			trace.setComponentProcessRequest(
				ucdComponentProcessRequest
			)
		}
		
		// Recursively process the children.
		if (trace.getChildren()) {
			for (childTrace in trace.getChildren()) {
				getRequestTrace(childTrace)
			}
		}			
	}
}
