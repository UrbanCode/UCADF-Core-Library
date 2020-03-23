/**
 * This enumeration represents the trace type values.
 */
package org.urbancode.ucadf.core.model.ucd.processRequest

import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum UcdProcessRequestTraceTypeEnum {
	// This is only the subset of possible values that are needed by the UCADF processing.
	APPLICATIONMANUALTASK("applicationManualTask"),
	COMPONENTMANUALTASK("componentManualTask")
	
	private String value
	
	// Constructor.	
	UcdProcessRequestTraceTypeEnum(final String value) {
		this.value = value
	}

	/** The enumeration to create for deserialization. Gets a new enumeration by matching either the enumeration name or the value. */
	@JsonCreator
	public static UcdProcessRequestTraceTypeEnum newEnum(final String value) {
		UcdProcessRequestTraceTypeEnum newEnum = UcdProcessRequestTraceTypeEnum.values().find {
			(it.name() == value.toUpperCase() || it.getValue() == value)
		}
		
		if (!newEnum) {
			throw new UcAdfInvalidValueException("Process request trace type enumeration [$value] is invalid.")
		}
		
		return newEnum
	}
	
	/** Get the process request trace type value. This is the value to use for serialization. */
	@JsonValue
	public String getValue() {
		return value
	}
}
