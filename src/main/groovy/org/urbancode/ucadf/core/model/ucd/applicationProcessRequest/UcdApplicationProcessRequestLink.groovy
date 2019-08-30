/**
 * This class is used to provide process request link values.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcessRequest

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcdApplicationProcessRequestLink extends UcdObject {
	/** The link title. */
	String title
	
	/** The link value. */
	String value
	
	// Constructors.	
	UcdApplicationProcessRequestLink() {
	}
}
