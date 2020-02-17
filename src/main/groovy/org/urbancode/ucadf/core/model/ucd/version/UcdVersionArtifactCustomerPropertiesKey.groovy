/**
 * This class instantiates a version artifact customer properties key object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersionArtifactCustomerPropertiesKey extends UcdObject {
	String name
	String value
	
	// Constructors.	
	UcdVersionArtifactCustomerPropertiesKey() {
	}
}
