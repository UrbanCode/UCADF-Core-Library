/**
 * This class instantiates a generic process request object.
 */
package org.urbancode.ucadf.core.model.ucd.genericProcessRequest

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTrace

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdGenericProcessRequest extends UcdObject {
	/** The generic process request ID. */
	String id
	
	/** The process. */
	UcdGenericProcess process
	
	/** The procuess request trace. */
	UcdProcessRequestTrace trace
	
	/** The context properties. TODO: Needs class? */
	List contextProperties
	
	/** The submitted time. */
	Long submittedTime
	
	/** The associated workflow trace ID. */
	String workflowTraceId
	
	/** The user that submitted the process. */
	String userName

	/** The process path. */	
	String processPath
	
	/** The process version. */
	Long processVersion
	
	/** The start time. */
	Long startTime
	
	/** The request response result. */
	UcdProcessRequestResponseResultEnum result
	
	/** The request response state. TODO: Enumeration? */
	String state
	
	/** The request response paused. */
	String paused
	
	// Constructors.	
	UcdGenericProcessRequest() {
	}
}
