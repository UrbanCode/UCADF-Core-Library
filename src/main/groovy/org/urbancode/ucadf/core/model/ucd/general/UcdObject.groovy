/**
 * The abstract UCD object class that provides useful methods to all subclasses.
 */
package org.urbancode.ucadf.core.model.ucd.general

import org.urbancode.ucadf.core.model.ucadf.UcAdfObject

abstract class UcdObject extends UcAdfObject {
	/**
	 * Determine if a string is a ID.	
	 * @param value The value to examine.
	 * @return True if the value is a ID.
	 */
	public static Boolean isUUID(final String value) {
		return (value ==~ /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/)
	}

	/**
	 * Determine if a sting will be interpreted as a GUID due to a UCD bug.
	 * @param value The value to examine.
	 * @return True if the value is is incorrectly interpreted as a UUID.
	 */
	public static Boolean isIncorrectlyInterpretedAsUUID(final String value) {
		return (value ==~ /^[0-9a-fA-F]+-[0-9a-fA-F]+-[0-9a-fA-F]+-[0-9a-fA-F]+-[0-9a-fA-F]+$/)
	}
}
