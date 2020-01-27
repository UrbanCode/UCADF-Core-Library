/**
 * This enumeration represents the agent status values.
 */
package org.urbancode.ucadf.core.model.ucd.agent

enum UcdAgentStatusEnum {
	/** The agent is connecting. */
	CONNECTING,
	
	/** The agent is in an error condition. */
	ERROR,
	
	/** The agent is online. */
	ONLINE,
	
	/** The agent is offline. */
	OFFLINE,
	
	/** The agent should be upgraded. */
	UPGRADE,
	
	/** The agent upgrade is recommended. */
	UPGRADE_RECOMMND
	
	// TODO: Any additional statuses?
}
