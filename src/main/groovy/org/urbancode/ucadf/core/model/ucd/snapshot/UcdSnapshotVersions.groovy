/**
 * This class instantiates snapshot version objects.
 */
package org.urbancode.ucadf.core.model.ucd.snapshot

import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSnapshotVersions extends UcdComponent {
	/** The desired versions. */
	List<UcdVersion> desiredVersions
	
	// Constructors.	
	UcdSnapshotVersions() {
	}
}
