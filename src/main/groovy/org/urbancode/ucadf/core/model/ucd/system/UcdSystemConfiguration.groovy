/**
 * This class instantiates a system configuration object.
 */
package org.urbancode.ucadf.core.model.ucd.system

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdSystemConfiguration extends UcdObject {
	String vendorName
	String externalURL
	String externalUserURL
	Long repoAutoIntegrationPeriod
	String deployMailHost
	String deployMailPassword
	Long deployMailPort
	Boolean deployMailSecure
	String deployMailSender
	String deployMailUsername
	Long snapshotDaysToKeep
	Long cleanupHourOfDay
	Long cleanupDaysToKeep
	Long cleanupCountToKeep
	String cleanupArchivePath
	Long historyCleanupHour
	Long historyCleanupMinute
	Long historyCleanupDaysToKeep
	Long historyCleanupDuration
	Boolean historyCleanupEnabled
	Boolean auditLogReadEntriesEnabled
	Boolean auditLogCleanupEnabled
	Long auditLogCleanupHour
	Long auditLogCleanupMinute
	Long auditLogRetentionLength
	Boolean enableInactiveLinks
	Boolean enablePromptOnUse
	Boolean enableAllowFailure
	Boolean validateAgentIp
	Boolean skipCollectPropertiesForExistingAgents
	Boolean requireComplexPasswords
	Long minimumPasswordLength
	Boolean enableUIDebugging
	String messageOfTheDay
	Boolean isCreateDefaultChildren
	Boolean requireCommentForProcessChanges
	Boolean requireProcessPromotionApproval
	Boolean failProcessesWithUnresolvedProperties
	Boolean enforceDeployedVersionIntegrity
	Boolean copyToCodestation
	Long discoveryExpiry
	Long configureExpiry
	Long importExpiry
	Long pluginStepExpiry
	Boolean useDefaultATRIfNotSpecified
	String artifactAgent
	String artifactAgentName
	String licenseServerUrl
	String licenseMode
	String agentAutoLicenseType
	String defaultLocale
	String defaultSnapshotLockType
	Boolean envCompPropsOverrideEnvProps
	Boolean safeEditEnabled
	Boolean allowProcessLocking
	Boolean requireProcessLocking
	Boolean maintenanceModeEnabled
	Long keepMeLoggedInHours
	Boolean deleteEnvResources
	Boolean vcCompressionUpgradeEnabled
	Boolean vcCompressionUpgradeCompleted
	
	// Constructors.
	UcdSystemConfiguration() {
	}
}
