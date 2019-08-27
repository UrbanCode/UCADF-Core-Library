/**
 * This class represents a not found exception.
 */
package org.urbancode.ucadf.core.model.ucd.exception

class UcdNotFoundException extends UcdHandledException {
	// Constructors.
	UcdNotFoundException(final String message) {
		super(message)
	}
	
	UcdNotFoundException(final Exception e) {
		this(e.getMessage())
	}
}
