/**
 * This class instantiates a property sheet definition object.
 */
package org.urbancode.ucadf.core.model.ucd.property

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdPropSheetDef {
	/** The property sheet definition ID. */
	String id
	
	/** TODO: What's this? */
	Boolean versioned
	
	/** The URL to resolve HTTP values. */
	String resolveHttpValuesUrl
		
	/** The path. */
	String path
	
	/** The number of versions. */
	Long versionCount
	
	/** The current version. */
	Long version
	
	/** The flag that indicates commit. */
	Boolean commit
}
