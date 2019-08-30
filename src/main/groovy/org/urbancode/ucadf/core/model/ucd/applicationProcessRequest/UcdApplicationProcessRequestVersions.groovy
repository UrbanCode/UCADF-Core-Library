/**
 * This class instantiates application process request objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationProcessRequestVersions extends UcdObject {
	/** TODO: What is this? */
	Boolean futureRequest
	
	/** The list of version objects. */
	List<UcdVersion> versions
	
	// Constructors.	
	UcdApplicationProcessRequestVersions() {
	}
}
