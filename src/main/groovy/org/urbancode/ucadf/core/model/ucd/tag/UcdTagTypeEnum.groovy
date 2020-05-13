/**
 * This enumeration represents the tag type values.
 */
package org.urbancode.ucadf.core.model.ucd.tag

import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum UcdTagTypeEnum {
	AGENT("Agent"),
	APPLICATION("Application"),
	COMPONENT("Component"),
	RESOURCE("Resource")
	
	private String value
	
	// Constructor.	
	UcdTagTypeEnum(final String value) {
		this.value = value
	}

	/** The enumeration to create for deserialization. Gets a new enumeration by matching either the enumeration name or the value. */
	@JsonCreator
	public static UcdTagTypeEnum newEnum(final String value) {
		UcdTagTypeEnum newEnum = UcdTagTypeEnum.values().find {
			(it.name() == value.toUpperCase() || it.getValue() == value)
		}
		
		if (!newEnum) {
			throw new UcAdfInvalidValueException("Tag type enumeration [$value] is invalid.")
		}
		
		return newEnum
	}
	
	/** Get the resoure type value. This is the value to use for serialization. */
	@JsonValue
	public String getValue() {
		return value
	}
}
