/**
 * This class instantiates component process request task objects.
 */
package org.urbancode.ucadf.core.model.ucd.componentProcessRequest

import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.processRequest.UcdProcessRequestTask

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentProcessRequestTask extends UcdProcessRequestTask {
	/** The associated component process request. */
	UcdComponentProcessRequest componentProcessRequest
	
	/** The associated component. */
	UcdComponent component

	// Constructors.
	UcdComponentProcessRequestTask() {
	}
}
