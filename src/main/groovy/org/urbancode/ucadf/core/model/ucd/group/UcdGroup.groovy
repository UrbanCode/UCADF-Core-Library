/**
 * This class instantiates a group object.
 */
package org.urbancode.ucadf.core.model.ucd.group

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdGroup extends UcdObject {
	/** The application ID. */
	String id
	
	/** The name. */
	String name

	/** The flag that indicates the group is enabled. */	
	Boolean enabled
}
