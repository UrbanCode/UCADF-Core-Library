/**
 * This class instantiates a workflow process request object.
 */
package org.urbancode.ucadf.core.model.ucd.workflow

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.As

// Provides a way to deserialize the collection of process requests.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "processRequestType", visible=true)
@JsonSubTypes([
	@Type(value = UcdWorkflowApplicationProcessRequest.class, name = UcdWorkflowProcessRequest.TYPE_APPLICATION),
	@Type(value = UcdWorkflowComponentProcessRequest.class, name = UcdWorkflowProcessRequest.TYPE_COMPONENT),
	@Type(value = UcdWorkflowGenericProcessRequest.class, name = UcdWorkflowProcessRequest.TYPE_GENERIC)
])
interface UcdWorkflowProcessRequest {
	public final static String TYPE_APPLICATION = "application"
	public final static String TYPE_COMPONENT = "component"
	public final static String TYPE_GENERIC = "generic"
	
	public String getProcessRequestType()
}
