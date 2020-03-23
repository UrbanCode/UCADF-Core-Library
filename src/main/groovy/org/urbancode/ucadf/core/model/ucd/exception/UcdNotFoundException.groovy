/**
 * This class represents a not found exception.
 */
package org.urbancode.ucadf.core.model.ucd.exception

import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfHandledException

@Deprecated // Use UcAdfHandledException.
class UcAdfNotFoundException extends UcAdfHandledException {
	// Constructors.
	UcAdfNotFoundException(final String message) {
		super(message)
	}
	
	UcAdfNotFoundException(final Exception e) {
		this(e.getMessage())
	}
}
