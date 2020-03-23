/**
 * This enumeration represents the task response values.
 */
package org.urbancode.ucadf.core.model.ucd.task

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

enum UcdTaskResponseEnum {
	PASSED("passed"),
	FAILED("failed")
	
	private String value
	
	// Constructor.	
	UcdTaskResponseEnum(final String value) {
		this.value = value
	}

	/** The enumeration to create for deserialization. Gets a new enumeration by matching either the enumeration name or the value. */
	@JsonCreator
	public static UcdTaskResponseEnum newEnum(final String value) {
		UcdTaskResponseEnum newEnum = UcdTaskResponseEnum.values().find {
			(it.name() == value.toUpperCase() || it.getValue() == value)
		}
		
		if (!newEnum) {
			throw new UcAdfInvalidValueException("Task response enumeration [$value] is invalid.")
		}
		
		return newEnum
	}
	
	/** Get the task response value. This is the value to use for serialization. */
	@JsonValue
	public String getValue() {
		return value
	}
}
