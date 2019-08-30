/**
 * This enumeration represents the application process offline agent handling values.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcess

enum UcdApplicationProcessOfflineAgentHandlingEnum {
	/** The expected agents are checked for online status before a process is run. If agents are offline, the process does not run. */
	PRE_EXECUTION_CHECK,
	
	/** The process runs while at least one agent is online and reports any failed deployments because of offline agents. */
	FAIL_BUT_CONTINUE,
	
	/** The process runs while at least one agent is online; reports successful deployments. */
	ALLOW_OFFLINE
}
