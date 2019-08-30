/**
 * This class instantiates component process request objects.
 */
package org.urbancode.ucadf.core.model.ucd.componentProcessRequest

import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTrace

class UcdComponentProcessRequest extends UcdProcessRequestTrace {
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
