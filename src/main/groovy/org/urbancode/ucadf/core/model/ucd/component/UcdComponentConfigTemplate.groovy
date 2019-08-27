/**
 * This class instantiates component configuration template objects.
 */
package org.urbancode.ucadf.core.model.ucd.component

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentConfigTemplate extends UcdObject {
	/** The component configuration template ID. */
	String id
	
	/** The name. */
	String name

	/** The data. */	
	String data
	
	/** The component ID. */
	String componentId
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	/** The flag that indicates commit. */
	Long commit
	
	/** The path. */
	String path
	
	// Constructors.	
	UcdComponentConfigTemplate() {
	}
}
