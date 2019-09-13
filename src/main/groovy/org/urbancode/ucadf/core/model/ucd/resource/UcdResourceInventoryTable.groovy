/**
 * This class instantiates a table of token objects.
 */
package org.urbancode.ucadf.core.model.ucd.resource

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

//@JsonIgnoreProperties(ignoreUnknown = true)
class UcdResourceInventoryTable extends UcdObject {
	/** The total records. */
	Long totalRecords

	/** The list of resource inventory objects. */	
	List<UcdResourceInventory> records
	
	// Constructors.	
	UcdResourceInventoryTable() {
	}
}
