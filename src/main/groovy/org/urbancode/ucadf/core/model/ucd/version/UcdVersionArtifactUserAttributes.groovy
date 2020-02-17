/**
 * This class instantiates a version artifact user attributes object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersionArtifactUserAttributes extends UcdObject {
	String zOSFileUserAttributesVersion
	List<UcdVersionArtifactCustomerPropertiesKey> zOSCustomerPropertiesKey
	UcdVersionArtifactResourceInputsKey zOSResourceInputsKey
	
	// Constructors.	
	UcdVersionArtifactUserAttributes() {
	}
}
