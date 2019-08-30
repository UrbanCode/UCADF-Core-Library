/**
 * This class instantiates environment component inventory objects.
 */
package org.urbancode.ucadf.core.model.ucd.environment

import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdEnvironmentComponentInventory extends UcdVersion {
	/** The children. TODO: Need a class. */
	List<Object> children
	
	// Constructors.
	UcdEnvironmentComponentInventory() {
	}
}
