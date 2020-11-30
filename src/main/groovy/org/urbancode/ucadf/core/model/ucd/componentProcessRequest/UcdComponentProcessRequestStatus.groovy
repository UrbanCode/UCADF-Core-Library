/**
 * This class instantiates component process request objects.
 */
package org.urbancode.ucadf.core.model.ucd.componentProcessRequest

import org.urbancode.ucadf.core.model.ucadf.UcAdfObject
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseStatusEnum

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentProcessRequestStatus extends UcAdfObject {
	/** The request ID. */
	String requestId = ""
	
	/** The request response result. */
	UcdProcessRequestResponseResultEnum result
	
	/** The request response state. */
	UcdProcessRequestResponseStatusEnum state
}
