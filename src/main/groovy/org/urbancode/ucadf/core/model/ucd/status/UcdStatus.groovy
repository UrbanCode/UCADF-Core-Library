/**
 * This class instantiates status objects.
 */
package org.urbancode.ucadf.core.model.ucd.status

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdStatus extends UcdObject {
	/** The status ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The type. */
	UcdStatusTypeEnum type

	/** The color. This is synonymous with {@link UcdColorEnum} but is not deserialized with that enumeration because more values are possible. */	
	String color

	/** The flag that indicates only one status may exist on an entity. */	
	Boolean unique
	
	/** The flag that indicates the status has been deleted. */
	Boolean deleted
	
	/** The date created. */
	Long created
	
	/** The user that created. */
	String user
	
	// Constructors.	
	UcdStatus() {
	}
}
