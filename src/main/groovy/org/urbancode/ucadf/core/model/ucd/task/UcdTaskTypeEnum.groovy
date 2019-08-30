/**
 * This enumeration represents the task type values.
 */
package org.urbancode.ucadf.core.model.ucd.task

import com.fasterxml.jackson.annotation.JsonValue

// TODO: No actions yet for application or generic tasks.
enum UcdTaskTypeEnum {
	APPLICATIONTASK("applicationTask"),
	COMPONENTTASK("componentTask")
	
	private String value
	
	// Constructor.	
	UcdTaskTypeEnum(final String value) {
		this.value = value
	}

	/** Get the task type value. This is the value to use for serialization. */
	@JsonValue
	public String getValue() {
		return value
	}
}
