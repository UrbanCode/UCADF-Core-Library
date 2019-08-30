/**
 * This class instantiates a workflow component process request object.
 */
package org.urbancode.ucadf.core.model.ucd.workflow

import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdWorkflowComponentProcessRequest extends UcdComponentProcessRequest implements UcdWorkflowProcessRequest {
	String processRequestType = TYPE_COMPONENT
	
	// Constructors.	
	UcdWorkflowComponentProcessRequest() {
	}
}
