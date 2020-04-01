/**
 * This class instantiates a secure string object.
 */
package org.urbancode.ucadf.core.model.ucadf

import org.urbancode.ucadf.core.model.ucadf.objectmapper.UcAdfMasked

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcAdfSecureString extends UcAdfObject {
	// This will have the String class methods.
	@UcAdfMasked
	@Delegate 
	final String secureString

	@UcAdfMasked
	@Delegate
	final String bytes

	// Constructors.	
	UcAdfSecureString() {
		this("")
	}
	
	UcAdfSecureString(final String secureString) {
		this.secureString = secureString
	}

	/**
	 * Gets a string representation of the secure string.
	 * @return The string.	
	 */
	public String toString() {
		return secureString
	}
}
