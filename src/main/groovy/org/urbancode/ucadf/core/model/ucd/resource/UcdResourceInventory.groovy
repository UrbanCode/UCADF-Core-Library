/**
 * This class instantiates a table of token objects.
 */
package org.urbancode.ucadf.core.model.ucd.resource

import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.status.UcdStatus
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdResourceInventory extends UcdObject {
	/** The component. */
	UcdComponent component
	
	/** The version. */
	UcdVersion version
	
	/** The resource. */
	UcdResource resource

	/** The inventory date. */
	Long date
	
	/** The inventory status. */
	UcdStatus status

	/** The flag that indicates the inventory is deleted. */
	Boolean deleted	
	
	// Constructors.	
	UcdResourceInventory() {
	}
}
