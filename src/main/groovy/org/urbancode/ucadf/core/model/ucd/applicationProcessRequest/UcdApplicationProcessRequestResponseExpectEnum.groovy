/**
 * This enumeration represents the application process status expect values.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestResponseResultEnum

enum UcdApplicationProcessRequestResponseExpectEnum {
	SUCCESS(200, UcdProcessRequestResponseResultEnum.SUCCEEDED),
    GOODREQUEST(200, UcdProcessRequestResponseResultEnum.EMPTY),
	BADREQUEST(400),
	FAULTED(200, UcdProcessRequestResponseResultEnum.FAULTED)

	private Integer httpStatus
	private UcdProcessRequestResponseResultEnum responseResult
	
	// Constructor.	
	UcdApplicationProcessRequestResponseExpectEnum(
		final Integer httpStatus,
		final UcdProcessRequestResponseResultEnum responseResult = null) {

		this.httpStatus = httpStatus
		this.responseResult = responseResult
	}

	/** 
	 * Validates this enum matches the specifified response values.
	 * @param compareHttpStatus The HTTP status to compare to the enumeration.
	 * @param compareResponseResult The response status to compare to the enumeration.
	 */
	public void validate(
		final Integer compareHttpStatus,
		final UcdProcessRequestResponseResultEnum compareResponseResult) {	
		
		if (httpStatus != compareHttpStatus) {
			throw new UcAdfInvalidValueException("Expected [${name()}] application process request HTTP status [$compareHttpStatus] doesn't match value [$httpStatus].")
		}

		if (responseResult && (responseResult != compareResponseResult)) {
			throw new UcAdfInvalidValueException("Expected [${name()}] application process request response status [${compareResponseResult.getValue()}] doesn't match expected value [${responseResult.getValue()}].")
		}
	}
}
