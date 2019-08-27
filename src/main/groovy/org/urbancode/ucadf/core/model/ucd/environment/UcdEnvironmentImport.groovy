/**
 * This class is used to import environments.
 */
package org.urbancode.ucadf.core.model.ucd.environment

import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdEnvironmentImport {
	/** The environment name. */
	String name
	
	/** The description. */
	String description
	
	/** The color. This is synonymous with {@link UcdColorEnum} but is not deserialized with that enumeration because more values are possible. */
	String color

	/** The flag that indicates approvals are required. */	
	Boolean requireApprovals
	
	/** The list of exempt processes. TODO: Needs class. */
	List exemptProcesses
	
	/** The flag that indicates snapshots must be locked. */
	Boolean lockSnapshots
	
	/** The cleanup days to keep. */
	Long cleanupDaysToKeep
	
	/** The cleanup count to keep. */
	Long cleanupCountToKeep
	
	/** The order. */
	Long order
	
	/** The flag that indicates the environment is active. */
	Boolean active

	/** The property sheet. */	
	UcdPropSheet propSheet

	/** The list of base resources. TODO: Needs class. */	
	List baseResources
	
	/** The list of component property sheets. TODO: Needs class. */
	List componentPropSheets
	
	/** The list of version conditions. TODO: Needs class. */
	List versionConditions
	
	/** The list of team mappings. TODO: Needs class. */
	List teamMappings
	
	/** The flag that indicates no self approvals. */
	Boolean noSelfApprovals
	
	/** The flag that indicates snapshots are required. */
	Boolean requireSnapshots
	
	/** The flag that indicates enable process history cleanup. */
	Boolean enableProcessHistoryCleanup
	
	/** The flag that indicates use system default days. */
	Boolean useSystemDefaultDays
	
	/** The list of base resources. TODO: Needs class. */
	List baseResourcesFull
	
	/** The required role name. */
	String requiredRoleName
	
	/** The history cleanup days to keep. */
	Long historyCleanupDaysToKeep
	
	/** The template property sheet. */
	UcdPropSheet templatePropSheet
	
	// Constructors.
	UcdEnvironmentImport() {
	}
}
