/**
 * This class serializes and deserializes application process request objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTrace
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationProcessRequest extends UcdObject {
	/** The root trace object. */
	UcdProcessRequestTrace rootTrace
	
	/** The deployment request ID. */
	String deploymentRequestId
	
	/** The context Properties. */
	List<UcdProperty> contextProperties
	
	/** The process request ID. */
	String id
	
	/** The submitted time. */
	Long submittedTime
	
	/** The trace ID. TODO: What is this? */
	String traceId
	
	/** The user name. */
	String userName
	
	/** The flag that indicates only changed versions. */
	Boolean onlyChanged
	
	/** The description. */
	String description
	
	/** The start time. */
	Long startTime
	
	/** The end time. */
	Long endTime

	/** The duration. */
	Long duration

	/** The request result. */
	UcdProcessRequestResponseResultEnum result
	
	/** The request state (a.k.a the request status). */
	UcdApplicationProcessRequestResponseStatusEnum state
	
	/** The flag that indicates the process is paused. */
	Boolean paused
	
	/** The associated application object. */
	UcdApplication application
	
	/** The associated application process object. */
	UcdApplicationProcess applicationProcess
	
	/** The associated environment object. */
	UcdEnvironment environment
	
	/** The entry. TODO: What is this? */
	Map entry

	// Constructors.	
	UcdApplicationProcessRequest() {
	}
}
