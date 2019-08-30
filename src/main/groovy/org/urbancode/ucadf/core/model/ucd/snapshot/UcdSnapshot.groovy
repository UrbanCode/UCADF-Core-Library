/**
 * This class instantiates a snapshot object.
 */
package org.urbancode.ucadf.core.model.ucd.snapshot

import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshot extends UcdObject {
	/** The snapshot ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The created date. */	
	Long created
	
	/** The flag to indicate the snapshot is active. */
	Boolean active
	
	/** The flag to indicate the versions are locked. */
	Boolean versionsLocked
	
	/** The flag to indicate the configuration is locked. */
	Boolean configLocked
	
	/** The associated application ID. */
	String applicationId
	
	/** The associated application. */
	UcdApplication application
	
	/** The user that created the snapshot. */
	String user

	// Constructors.	
	UcdSnapshot() {
	}
}
