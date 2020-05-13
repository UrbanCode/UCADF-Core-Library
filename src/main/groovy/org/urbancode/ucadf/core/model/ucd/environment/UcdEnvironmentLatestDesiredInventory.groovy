/**
 * This class instantiates environment resource inventory objects.
 */
package org.urbancode.ucadf.core.model.ucd.environment

import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.status.UcdStatus
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdEnvironmentLatestDesiredInventory extends UcdObject {
	String id
	String deploymentRequestId
	UcdEnvironment environment
	UcdVersion version
	UcdComponent component
	UcdEnvironmentCompliancy compliancy
	Long date
	UcdStatus status
	Long propVersion
	List<UcdEnvironmentLatestDesiredInventory> additionalVersions
	
	// Constructors.
	UcdEnvironmentLatestDesiredInventory() {
	}
}
