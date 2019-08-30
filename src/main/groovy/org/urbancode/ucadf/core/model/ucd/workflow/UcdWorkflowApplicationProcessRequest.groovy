/**
 * This class instantiates a workflow application process request object.
 */
package org.urbancode.ucadf.core.model.ucd.workflow

import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdWorkflowApplicationProcessRequest extends UcdApplicationProcessRequest implements UcdWorkflowProcessRequest {
	String processRequestType = TYPE_APPLICATION

	// Constructors.	
	UcdWorkflowApplicationProcessRequest() {
	}
}
