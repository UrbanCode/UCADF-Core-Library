/**
 * This class instantiates security type permissions.
 * <p>
 * The UCD APIs refer to this as a security action but the UCADF libraries are using referring to this as a security permission.
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSecurityTypePermission extends UcdObject {
	/** The security type permission ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The category. */	
	String category
	
	/** The permission (known to UCD API as the action). */
	@JsonProperty("action")
	UcdSecurityPermission permission
	
	/** The security subtype (known to UCD API as resourceRole */
	@JsonProperty("resourceRole")
	UcdSecuritySubtype subtype
	
	// Constructors.	
	UcdSecurityTypePermission() {
	}
}
