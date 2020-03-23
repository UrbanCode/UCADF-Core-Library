/**
 * This class represents a not found exception.
 */
package org.urbancode.ucadf.core.model.ucadf.exception

class UcAdfNotFoundException extends UcAdfHandledException {
	// Constructors.
	UcAdfNotFoundException(final String message) {
		super(message)
	}
	
	UcAdfNotFoundException(final Exception e) {
		this(e.getMessage())
	}
}
