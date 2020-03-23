/**
 * This class represents an unhandled exception.
 */
package org.urbancode.ucadf.core.model.ucadf.exception

import javax.ws.rs.core.Response

class UcAdfHandledException extends Exception {
	private String message
	
	// Constructors.
	UcAdfHandledException(final String message) {
		super(message, null, true, true)
		this.message = message
	}

	/**
	 * Print a stack trace.
	 */
	@Override
	public void printStackTrace() {
		// Output the error message.
		println "=".multiply(80) + "\nException: ${message}\n" + "=".multiply(80)
		
		// Find the first element of the stack trace that reports a relevant line.
		StackTraceElement[] elements = getStackTrace()
		for (element in elements) {
			if (element.getClassName() ==~ /org.urbancode.*/) {
				println this.getClass().getSimpleName() + " " + element
			}
		}
	}

	/**
	 * Get an error message containing information from a web target response.	
	 * @param response The response object.
	 * @return The error message.
	 */
	public static String getResponseErrorMessage(final Response response) {
		return "${response.getStatus()} ${response.readEntity(String.class)}"
	}
}
