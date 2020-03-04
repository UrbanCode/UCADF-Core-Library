/**
 * This action gets a process request trace.
 */
package org.urbancode.ucadf.core.action.ucd.workflow

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.applicationProcessRequest.UcdGetApplicationProcessRequest
import org.urbancode.ucadf.core.action.ucd.componentProcessRequest.UcdGetComponentProcessRequest
import org.urbancode.ucadf.core.action.ucd.genericProcessRequest.UcdGetGenericProcessRequest
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcessRequest.UcdGenericProcessRequest
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTrace
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

// TODO: This is not complete and does not currently return anything.
class UcdGetProcessRequestTrace extends UcAdfAction {
	// Action properties.
	/** The request ID. */
	String requestId
	
	/** The flag that indicates fail if the process request is not found. Default is true. */
	Boolean failIfNotFound = true

	/**
	 * Runs the request trace.	
	 */
	@Override
	public UcdProcessRequestTrace run() {
		// Validate the action properties.
		validatePropsExist()

		UcdProcessRequestTrace requestTrace
		
		Boolean foundProcess = false
		
		// Determine if request ID is associated with a running application process.
		UcdApplicationProcessRequest appProcessRequest = actionsRunner.runAction([
			action: UcdGetApplicationProcessRequest.getSimpleName(),
//			actionInfo: false,
//			actionVerbose: false,
			requestId: requestId,
			failIfNotFound: false
		])
		
		if (appProcessRequest) {
			foundProcess = true
			
			// Recursively process the child traces.			
			requestTrace = appProcessRequest.getRootTrace()
			
			// Fill in the details of the request tract.
			getRequestTrace(requestTrace)
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

				requestTrace = compProcessRequest

				// Fill in the details of the request tract.
				getRequestTrace(requestTrace)
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
					
					requestTrace = genericProcessRequest.getTrace()
								
					// Fill in the details of the request tract.
					getRequestTrace(requestTrace)
				}
			}
		}
		
		if (!foundProcess && failIfNotFound) {
			throw new UcdInvalidValueException("Process request [$requestId] not found.")
		}

		return requestTrace
	}
	
	// Get the process request trace information.
	public getRequestTrace(UcdProcessRequestTrace trace) {
		logDebug("[${trace.getDisplayName()}] [${trace.getId()}] workflowTraceId [${trace.getWorkflowTraceId()}] componentProcessRequestId [${trace.getComponentProcessRequestId()}]")
		
		// Get the workflow step logs.
		if (trace.getWorkflowTraceId()) {
			if (trace.getExtraLogs() != null || "plugin".equals(trace.getType())) {
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
				actionInfo: false,
				requestId: trace.getComponentProcessRequestId()
			])

			trace.setComponentProcessRequest(
				ucdComponentProcessRequest
			)
			
			getRequestTrace(ucdComponentProcessRequest)
		}
		
		// Recursively process the children.
		if (trace.getChildren()) {
			for (childTrace in trace.getChildren()) {
				getRequestTrace(childTrace)
			}
		}
	}
}
