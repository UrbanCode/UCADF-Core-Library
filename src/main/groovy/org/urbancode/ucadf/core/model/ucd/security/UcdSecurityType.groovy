/**
 * This class instantiates security type objects.
 * <p>
 * The UCD APIs refer to this as "resourceType" but the UCADF libraries are using referring to this as a security type.
 */
package org.urbancode.ucadf.core.model.ucd.security

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSecurityType extends UcdObject {
	/** The security type ID. */
	String id
	
	/** The name. */
	String name
	

	// Constructors.	
	UcdSecurityType() {
	}
	
	/**
	 * @return Returns true if the type is allowed to have subtypes.
	 */
	public Boolean getSubtypeAllowed() {
		return UcdSecurityTypeEnum.newEnum(name).getSubtypeAllowed()
	}
}
