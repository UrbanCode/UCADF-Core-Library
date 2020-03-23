/**
 * This class instantiates a secure string object.
 */
package org.urbancode.ucadf.core.model.ucd.general

import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Deprecated // Use UcAdfSecureString.
@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSecureString extends UcAdfSecureString {
	// Constructors.	
	UcdSecureString() {
		super()
	}
	
	UcdSecureString(final String secureString) {
		super(secureString)
	}
}
