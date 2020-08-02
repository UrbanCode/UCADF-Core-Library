/**
 * This class instantiates a UCD global message object.
 */
package org.urbancode.ucadf.core.model.ucd.system

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdGlobalMessage extends UcdObject {
	String message
	String messageId
	String priority
	Boolean canDismiss
	String dismissId
	
	// Constructors.	
	UcdGlobalMessage() {
	}
}
