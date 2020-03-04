/**
 * This class instantiates a process request trace object.
 */
package org.urbancode.ucadf.core.model.ucd.processRequest

import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequestTask
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// TODO: This may need to be refactoried or extended for traces of the various types.
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdProcessRequestTrace extends UcdObject {
	// These were in all traces.
	/** The request ID. */
	String id
	
	/** The name. */
	String name
	
	/** The request type. This is synonymous with {@link UcdProcessReqestTraceTypeEnum} but is not deserialized with that enumeration because more values are possible. */
	String type

	/** The response state. */	
	UcdProcessRequestResponseStatusEnum state
	
	/** The response result. */
	UcdProcessRequestResponseResultEnum result

	/** The start date. */
	Long startDate
	
	/** The end date. */
	Long endDate

	/** The duration. */	
	Long duration

	/** The flag that indicates paused. */	
	Boolean paused

	/** The associated workflow trace ID. */	
	String workflowTraceId

	/** The child traces. */	
	List<UcdProcessRequestTrace> children
	
	String status

	// Component-related.
	UcdComponent component
	UcdComponentProcess componentProcess
	String componentProcessRequestId
	UcdComponentProcessRequest componentProcessRequest
	UcdComponentProcessRequestTask task
	
	// These were in children traces.
	String displayName
	Long graphPosition
	String propertyName
	Map fault
	String specialNameType
	UcdResource resource
	String lockName
	String value
	Object role
	String logText

	// These were in component process traces.
	String workingDir
	Map command
	List extraLogs

	// Added this to read the workflow step properties.
	List<UcdProperty> stepProperties
	List<UcdProperty> stepOutputProps
	
	// Constructors.	
	UcdProcessRequestTrace() {
	}
}
