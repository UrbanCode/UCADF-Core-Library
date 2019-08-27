/**
 * This class instantiates property sheets.
 */
package org.urbancode.ucadf.core.model.ucd.property

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdPropSheet extends UcdObject {
	/** The property sheet ID. */
	String id
	
	/** The name. */
	String name
			
	/** The path. */
	String path
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version

	/** The commit. */	
	Long commit
	
	/** The flag that indicates versioned. */
	Boolean versioned
	
	/** The created date. */
	Long created
	
	/** The flag that indicates enforce complete snapshots. */
	Boolean enforceCompleteSnapshots
	
	/** The flag that indicates active. */
	Boolean active
	
	/** The list of tags. */
	List<UcdTag> tags
	
	/** The flag that indicates deleted. */
	Boolean deleted
	
	/** The user. */
	String user
	
	/** The list of properties. */
	List<UcdProperty> properties
	
	/** The security resource ID. */
	String securityResourceId

	UcdPropSheet() {
	}
}
