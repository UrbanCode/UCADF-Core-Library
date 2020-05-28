/**
 * This class instantiates a tag object.
 */
package org.urbancode.ucadf.core.model.ucd.tag

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdTag extends UcdObject {
	/** The tag ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The color. This is synonymous with {@link UcdColorEnum} but is not deserialized with that enumeration because more values are possible. */	
	String color

	/** The object type. */
	UcdTagTypeEnum objectType
	
	// Constructors.	
	UcdTag() {
	}
}
