/**
 * This class instantiates a workflow generic process request object.
 */
package org.urbancode.ucadf.core.model.ucd.workflow

import org.urbancode.ucadf.core.model.ucd.genericProcessRequest.UcdGenericProcessRequest
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdWorkflowGenericProcessRequest extends UcdGenericProcessRequest implements UcdWorkflowProcessRequest {
	/** The process request type. */
	String processRequestType = TYPE_GENERIC
	
	/** TODO: What's this? */
	Long warningCount

	/** TODO: What's this? */
	Boolean executable
	
	/** TODO: What's this? */
	UcdResource resource
	
	// Constructors.	
	UcdWorkflowGenericProcessRequest() {
	}
}
