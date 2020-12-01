/**
 * This class instantiates component process request objects.
 */
package org.urbancode.ucadf.core.model.ucd.componentProcessRequest

import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTrace

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentProcessRequest extends UcdProcessRequestTrace {
	// Common process properties.
	public final static String PROPNAME_REQUEST_ID = "request.id"
	public final static String PROPNAME_PARENTREQUEST_ID = "parentRequest.id"
	
	/** The submitted time. */
	Long submittedTime
	
	/** The trace ID. */
	String traceId
	
	/** The user name. */
	String userName
	
	/** The login name. */
	String loginName
	
	/** The parent request ID. */
	String parentRequestId
	
	/** The deployment request ID. */
	String deploymentRequestId
	
	/** The start time. */
	Long startTime

	/** The associated application. */
	UcdApplication application
		
	/** The associated environment. */
	UcdEnvironment environment
	
	/** The associated agent. */
	UcdAgent agent
	
	/** TODO: What's this? */
	Object entry
	
	// Constructors.	
	UcdComponentProcessRequest() {
	}
	
	/**
	 * Find a child task with a specific task name.
	 * @param taskName The task name.
	 * @return The cmoponent process request task.
	 */
	public UcdComponentProcessRequestTask findChildTask(final String taskName) {
		UcdComponentProcessRequestTask foundTask
		for (child in getChildren()) {
			UcdComponentProcessRequestTask task = child.getTask()
			if (task && task.getName() == taskName) {
				foundTask = task
				break
			}
		}
		return foundTask
	}
}
