/**
 * This class represents an invalid value exception.
 */
package org.urbancode.ucadf.core.model.ucd.exception

import javax.ws.rs.core.Response

class UcdInvalidValueException extends UcdHandledException {
	// Constructors.
	UcdInvalidValueException(final String message) {
		super(message)
	}
	
	UcdInvalidValueException(final Exception e) {
		this(e.getMessage())
	}

	// A web response exception.
	UcdInvalidValueException(final Response response) {
		this(getResponseErrorMessage(response))
	}
}
