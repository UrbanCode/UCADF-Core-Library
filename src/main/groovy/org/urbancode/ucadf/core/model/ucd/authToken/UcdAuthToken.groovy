/**
 * This class instantiates authentication token objects.
 */
package org.urbancode.ucadf.core.model.ucd.authToken

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdAuthToken extends UcdObject {
	/** The authentication token ID. */
	String id
	
	/** The associated user ID. */
	String userId
	
	/** The associated user name. */
	String userName
	
	/** The token value. (Always masked.) */
	String token

	/** The description. */	
	String description
	
	/** The expiration date. */
	String expiration
	
	/** The allowed IP addresses. */
	String host
	
	/** The created date. */
	Long createdDate
	
	// Constructors.	
	UcdAuthToken() {
	}
}
