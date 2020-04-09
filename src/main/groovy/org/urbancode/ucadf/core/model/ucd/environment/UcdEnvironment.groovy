/**
 * This class instantiates environment objects.
 */
package org.urbancode.ucadf.core.model.ucd.environment

import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.general.UcdColorEnum
import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdEnvironment extends UcdSecurityTypeObject {
	/** The environment ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The color. This is synonymous with {@link UcdColorEnum} but is not deserialized with that enumeration because more values are possible. */
	String color

	/** The flag that indicates require approvals. */	
	Boolean requireApprovals
	
	/** The flag that indicates lock snapshots. */
	Boolean lockSnapshots
	
	/** The calendar ID. */
	String calendarId
	
	/** The flag that indicates the environment is active. */
	Boolean active
	
	/** The flag to indicate the environment is deleted. */
	Boolean deleted
	
	/** The cleanup days to keep. */
	Long cleanupDaysToKeep
	
	/** The cleanup count to keep. */
	Long cleanupCountToKeep
	
	/** The conditions. */
	List conditions
	
	/** The compliancy. */
	UcdEnvironmentCompliancy compliancy
	
	/** The system cleanup days to keep. */
	Long systemCleanupDaysToKeep
	
	/** The system cleanup count to keep. */
	Long systemCleanupCountToKeep
	
	/** The exempt processes. */
	String exemptProcesses
	
	/** The flag that indicates no self approvals. */
	Boolean noSelfApprovals
	
	/** The snapshot days to keep. */
	Long snapshotDaysToKeep
	
	/** The flag that indicates enable process history cleanup. */
	Boolean enableProcessHistoryCleanup
	
	/** The flag that indicates use system default days. */
	Boolean useSystemDefaultDays
	
	/** The history cleanup days to keep. */
	Long historyCleanupDaysToKeep
	
	/** The flag that indicates require snapshot. */
	Boolean requireSnapshot

	/** The property sheet. */	
	UcdPropSheet propSheet

	/** The property sheet. */	
	UcdPropSheet templatePropSheet
	
	/** The properties. */
	List<UcdProperty> properties
	
	/** The flag that indicates inherit system cleanup. */
	Boolean inheritSystemCleanup
	
	/** The associated application. */
	UcdApplication application
	
	/** The associated latest snapshot. */
	UcdSnapshot latestSnapshot
	
	/** The flag that indicates snapshot compliant. */
	Boolean snapshotCompliant
	
	/** The security resource ID. */
	String securityResourceId

	/** The security properties. */
	UcdSecurityPermissionProperties security
	
	/** The extended security. */
	UcdExtendedSecurity extendedSecurity

	// Constructors.	
	UcdEnvironment() {
	}
}
