/**
 * This class has some common constants.
 */
package org.urbancode.ucadf.core.model.ucd.general

class UcdConstants {
	// End of line regular expression.
	public final static String EOL_REGEX = "\r?\n"
	public final static String COMMA_EOL_REGEX = ",|\r?\n"

	// Milliseconds.
    public final static long ONE_SECOND_IN_MILLIS = 1000
    public final static long ONE_MINUTE_IN_MILLIS = 60 * ONE_SECOND_IN_MILLIS
	
	public final static String USERNAME_ADMIN = "admin"
	public final static String ROLENAME_ADMINISTRATOR = "Administrator"
}
