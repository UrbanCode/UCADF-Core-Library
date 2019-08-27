/**
 * This enumeration represents the resource type values.
 */
package org.urbancode.ucadf.core.model.ucd.resource

import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum UcdResourceTypeEnum {
	AGENT("agent"),
	AGENTPOOL("agent pool"),
	SUBRESOURCE("subresourced")
	
	private String value
	
	// Constructor.	
	UcdResourceTypeEnum(final String value) {
		this.value = value
	}

	/** The enumeration to create for deserialization. Gets a new enumeration by matching either the enumeration name or the value. */
	@JsonCreator
	public static UcdResourceTypeEnum newEnum(final String value) {
		UcdResourceTypeEnum newEnum = UcdResourceTypeEnum.values().find {
			(it.name() == value.toUpperCase() || it.getValue() == value)
		}
		
		if (!newEnum) {
			throw new UcdInvalidValueException("Resource type enumeration [$value] is invalid.")
		}
		
		return newEnum
	}
	
	/** Get the resoure type value. This is the value to use for serialization. */
	@JsonValue
	public String getValue() {
		return value
	}
}
