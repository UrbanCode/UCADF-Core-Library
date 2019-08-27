/**
 * This class instantiates environment resource inventory objects.
 */
package org.urbancode.ucadf.core.model.ucd.environment

import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdEnvironmentResourceInventory extends UcdResource {
	/** The children. TODO: Needs class but not sure what it is. */
	List<Object> children
	
	// Constructors.
	UcdEnvironmentResourceInventory() {
	}
}
