/**
 * This class instantiates security permission objects.
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSecurityPermission extends UcdObject {
	/** The security permission ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The category. */	
	String category

	// Constructors.	
	UcdSecurityPermission() {
	}
}
