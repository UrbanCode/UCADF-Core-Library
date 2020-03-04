/**
 * This enumeration represents the process request response status values.
 */
package org.urbancode.ucadf.core.model.ucd.processRequest

import com.fasterxml.jackson.annotation.JsonValue

enum UcdProcessRequestResponseStatusEnum {
	EMPTY(""),
	CANCELING("CANCELING"),
	CLOSED("CLOSED"),
	COMPENSATING("COMPENSATING"),
    EXECUTING("EXECUTING"),
	FAULTED("FAULTED"),
	FAULTING("FAULTING"),
    INITIALIZED("INITIALIZED"),
	PENDING("PENDING"),
	UNINITIALIZED("UNINITIALIZED")
	
	private String value
	
	// Constructor.	
	UcdProcessRequestResponseStatusEnum(final String value) {
		this.value = value
	}

	/** Get the process request response status value. This is the value to use for serialization. */
	@JsonValue
	public String getValue() {
		return value
	}
}
