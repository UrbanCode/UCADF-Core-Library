/**
 * This class instantiates security subtype objects.
 * <p>
 * The UCD APIs refer to this as "resourceRole" but the UCADF libraries are using referring to this as a security subtype.
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSecuritySubtype extends UcdObject {
	/** The security subtype ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The security type to which this subtype belongs. */
	UcdSecurityType resourceType
	
	// Constructors.	
	UcdSecuritySubtype() {
	}
}
