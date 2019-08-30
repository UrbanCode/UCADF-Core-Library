/**
 * This class instantiates resource tree objects.
 * Uses the same class recursively to represent the resource tree. The top node has records.
 */
package org.urbancode.ucadf.core.model.ucd.resource

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdResourceTree extends UcdResource {
	/** The top node has records. */
	List<UcdResourceTree> records
	
	/** The child resource tree. */
	UcdResourceTree resource
	
	/** The versions. TODO: What's this? */
	List<Object> versions
	
	/** The matches filters. TODO: What's this? */
	Object matchesFilters
	
	// Constructors.
	UcdResourceTree() {
	}
}
