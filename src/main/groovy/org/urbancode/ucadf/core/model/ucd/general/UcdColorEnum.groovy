/**
 * This enumeration represents the standard color values.
 */
package org.urbancode.ucadf.core.model.ucd.general

import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import com.fasterxml.jackson.annotation.JsonCreator

enum UcdColorEnum {
	RED("#D9182D"),
	ORANGE("#DD731C"),
	YELLOW("#FFCF01"),
	GREEN("#17AF4A"),
	TEAL("#007670"),
	BLUE("#00B2EF"),
	DARKRED("#A91024"),
	DARKORANGE("#B8461B"),
	DARKYELLOW("#FDB813"),
	DARKGREEN("#008A52"),
	DARKTEAL("#006059"),
	DARKBLUE("#00648D"),
	BLUE2("#008ABF"),
	LIGHTPURPLE("#AB1A86"),
	LIGHTPINK("#F389AF"),
	TAUPE("#838329"),
	GRAY("#6D6E70"),
	GRAY2("#83827F"),
	DARKBLUE2("#003F69"),
	PURPLE("#7F1C7D"),
	PINK("#EE3D96"),
	DARKTAUPE("#594F13"),
	DARKGRAY("#404041"),
	DARKGRAY2("#605F5C")
	
	private String value
	
	// Constructor.	
	UcdColorEnum(final String value) {
		this.value = value
	}

	/** The enumeration to create for deserialization. Gets a new enumeration by matching either the enumeration name or the value. */
	@JsonCreator
	public static UcdColorEnum newEnum(final String value) {
		UcdColorEnum newEnum = UcdColorEnum.values().find {
			(it.name() == value.toUpperCase() || it.getValue() == value)
		}
		
		if (!newEnum) {
			throw new UcdInvalidValueException("Color enumeration [$value] is invalid.")
		}
		
		return newEnum
	}

	/**
	 * @return The color value.
	 */
	public String getValue() {
		return value
	}	
}
