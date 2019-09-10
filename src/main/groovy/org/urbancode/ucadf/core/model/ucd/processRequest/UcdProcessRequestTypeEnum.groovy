/**
 * This enumeration represents the application process request response status values.
 */
package org.urbancode.ucadf.core.model.ucd.processRequest

import com.fasterxml.jackson.annotation.JsonValue

enum UcdProcessRequestTypeEnum {
	APPLICATION("application"),
	COMPONENT("component"),
	GENERIC("generic")

	private String value
	
	// Constructor.	
	UcdProcessRequestTypeEnum(final String value) {
		this.value = value
	}

	/** Get the process request type result value. This is the value to use for serialization. */
	@JsonValue	
	public String getValue() {
		return value
	}
}
