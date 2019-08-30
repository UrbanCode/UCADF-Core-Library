/**
 * This enumeration represents the application process request response status values.
 */
package org.urbancode.ucadf.core.model.ucd.processRequest

import com.fasterxml.jackson.annotation.JsonValue

enum UcdProcessRequestResponseResultEnum {
	EMPTY(""),
	APPROVALREJECTED("APPROVAL REJECTED"),
	AWAITINGAPPROVAL("AWAITING APPROVAL"),
	CANCELED("CANCELED"),
	COMPENSATED("COMPENSATED"),
	FAILEDTOSTART("FAILED TO START"),
	FAULTED("FAULTED"),
	NONE("NONE"),
	SCHEDULEDFORFUTURE("SCHEDULED FOR FUTURE"),
	SUCCEEDED("SUCCEEDED"),
	UNINITIALIZED("UNINITIALIZED")

	private String value
	
	// Constructor.	
	UcdProcessRequestResponseResultEnum(final String value) {
		this.value = value
	}

	/** Get the application process request response result value. This is the value to use for serialization. */
	@JsonValue	
	public String getValue() {
		return value
	}
}
