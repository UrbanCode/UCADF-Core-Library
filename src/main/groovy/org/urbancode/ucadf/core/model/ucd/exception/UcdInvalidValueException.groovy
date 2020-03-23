/**
 * This class represents an invalid value exception.
 */
package org.urbancode.ucadf.core.model.ucd.exception

import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfHandledException

@Deprecated // Use UcAdfHandledException.
class UcAdfInvalidValueException extends UcAdfHandledException {
	// Constructors.
	UcAdfInvalidValueException(final String message) {
		super(message)
	}
	
	UcAdfInvalidValueException(final Exception e) {
		this(e.getMessage())
	}

	// A web response exception.
	UcAdfInvalidValueException(final Response response) {
		this(getResponseErrorMessage(response))
	}
}
