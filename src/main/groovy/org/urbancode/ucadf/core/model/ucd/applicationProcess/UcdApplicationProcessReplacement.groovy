/**
 * This class is used to provide an application process replacement from/to strings.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcess

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdApplicationProcessReplacement extends UcdObject {
	/** The from string value. */
	String from
	
	/** The to string value. */
	String to

	// Constructors.
	UcdApplicationProcessReplacement() {
	}
	
	UcdApplicationProcessReplacement(
		final String from, 
		final String to) {
		
		this.from = from
		this.to = to
	}
	
	/**
	 * Gets the default list of replacement values to use.
	 * This is used to copy application processes from template application while replacing certain values in the process body with special replacements made for secure properties in the process.
	 * @param fromApplication The from application.
	 * @param toApplication The to application.
	 * @return The list of replacements.
	 */
	public static List<UcdApplicationProcessReplacement> getDefaultReplaceList(
		final String fromApplication,
		final String toApplication) {
		
		List<UcdApplicationProcessReplacement> ucdApplicationProcessReplacements = []
		
		ucdApplicationProcessReplacements.add(
			new UcdApplicationProcessReplacement(
				fromApplication, 
				toApplication
			)
		)
		
		ucdApplicationProcessReplacements.add(
			new UcdApplicationProcessReplacement(
				/"value":\s*"\*\*\*\*"/, 
				/"value": ""/
			)
		)
		
		return ucdApplicationProcessReplacements
	}
}
