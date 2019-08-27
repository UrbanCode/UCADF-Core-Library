/**
 * This enumeration represents the application process status values.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcessRequest

import com.fasterxml.jackson.annotation.JsonValue

enum UcdApplicationProcessRequestStatusEnum {
	SUCCESS("Success"),
	FAILURE("Failure")
	
	private String value
	
	// Constructor.	
	UcdApplicationProcessRequestStatusEnum(final String value) {
		this.value = value
	}

	/** Get the application process request status value. This is the value to use for serialization. */
	@JsonValue	
	public String getValue() {
		return value
	}
}
