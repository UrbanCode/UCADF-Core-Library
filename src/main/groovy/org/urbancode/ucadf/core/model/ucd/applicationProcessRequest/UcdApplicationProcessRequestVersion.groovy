/**
 * This class is used to provide versions to an application process request.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationProcessRequestVersion extends UcdObject {
	/** The component name or ID. */
	String component
	
	/** The version name or ID */
	String version
	
	// Constructors.	
	UcdApplicationProcessRequestVersion() {
	}
}
