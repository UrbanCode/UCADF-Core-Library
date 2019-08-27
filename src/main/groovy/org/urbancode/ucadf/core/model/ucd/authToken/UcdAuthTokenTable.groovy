/**
 * This class instantiates a table of token objects.
 */
package org.urbancode.ucadf.core.model.ucd.authToken

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAuthTokenTable extends UcdObject {
	/** The total records. */
	Long totalRecords

	/** The list of authentication token objects. */	
	List<UcdAuthToken> records
	
	// Constructors.	
	UcdAuthTokenTable() {
	}
}
