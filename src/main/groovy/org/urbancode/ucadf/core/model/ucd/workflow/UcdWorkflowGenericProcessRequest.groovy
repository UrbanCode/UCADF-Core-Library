/**
 * This class instantiates a workflow generic process request object.
 */
package org.urbancode.ucadf.core.model.ucd.workflow

import org.urbancode.ucadf.core.model.ucd.genericProcessRequest.UcdGenericProcessRequest

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdWorkflowGenericProcessRequest extends UcdGenericProcessRequest implements UcdWorkflowProcessRequest {
	String processRequestType = TYPE_GENERIC
	
	// Constructors.	
	UcdWorkflowGenericProcessRequest() {
	}
}
