/**
 * This class instantiates environment template objects.
 */
package org.urbancode.ucadf.core.model.ucd.environmentTemplate

import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdEnvironmentTemplate extends UcdEnvironment {
	/** The associated application template ID. */
	String applicationTemplateId
		
	/** The associated resource template ID. */
	String resourceTemplateId
		
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
		
	/** The path. */
	String path
	
	/** The created date. */
	Long created
	
	// Constructors.
	UcdEnvironmentTemplate() {
	}
}
