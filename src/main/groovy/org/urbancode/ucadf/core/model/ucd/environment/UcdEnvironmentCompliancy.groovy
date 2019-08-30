/**
 * This class instantiates environment compliancy objects.
 */
package org.urbancode.ucadf.core.model.ucd.environment

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdEnvironmentCompliancy extends UcdObject {
	/** The number of missing. */
	Long missingCount
	
	/** The number of correct. */
	Long correctCount
	
	/** The number of desired. */
	Long desiredCount
	
	// Constructors.
	UcdEnvironmentCompliancy() {
	}
}
