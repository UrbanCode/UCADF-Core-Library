/**
 * This class instantiates application process request status objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseStatusEnum

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationProcessRequestStatus extends UcdObject {
	/** The request ID. */
	String requestId = ""
	
	/** The request result. */
	UcdProcessRequestResponseResultEnum result = UcdProcessRequestResponseResultEnum.EMPTY
	
	/** The request status. */
	UcdProcessRequestResponseStatusEnum status = UcdProcessRequestResponseStatusEnum.EMPTY

	/** This is not returned by the API but is in this class to be able to convey the overall application process status for {@link UcdWaitForApplicationProcessRequest}. */
	UcdApplicationProcessRequestStatusEnum applicationProcessStatus
	
	/** The duration. */
	Long duration
	
	// Constructors.	
	UcdApplicationProcessRequestStatus() {
	}
}
