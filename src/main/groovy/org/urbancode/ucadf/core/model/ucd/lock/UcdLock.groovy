/**
 * This class instantiates lock objects.
 */
package org.urbancode.ucadf.core.model.ucd.lock

import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdLock extends UcdObject {
	/** The lock ID. */
	String id
	
	/** The lock name. */
	String lockName
	
	/** The associated component process request object. This is null if the lock is for a generic process. */
	UcdComponentProcessRequest componentProcessRequest
	
	/** A map with an ID and a name. TODO: What's this? */
	Map<String, String> lockable
	
	// Constructors.
	UcdLock() {
	}
}
