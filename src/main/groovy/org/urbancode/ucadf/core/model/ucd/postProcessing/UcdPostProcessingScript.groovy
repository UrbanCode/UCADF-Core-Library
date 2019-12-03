/**
 * This class instantiates post-processing script objects.
 */
package org.urbancode.ucadf.core.model.ucd.postProcessing

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdPostProcessingScript extends UcdObject {
	/** The ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The body. */
	String body
	
	// Constructors.	
	UcdPostProcessingScript() {
	}
}
