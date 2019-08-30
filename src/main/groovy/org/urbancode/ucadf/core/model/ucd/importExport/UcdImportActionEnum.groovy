/**
 * This enumeration represents the import action values.
 */
package org.urbancode.ucadf.core.model.ucd.importExport

import com.fasterxml.jackson.annotation.JsonValue

enum UcdImportActionEnum {
	/** An import that is creating a new entity. */
	IMPORT("import"),
	
	/** An import that is upgrading an existing entity. */
	UPGRADE("upgrade")
	
	private String value
	
	// Constructor.	
	UcdImportActionEnum(final String value) {
		this.value = value
	}

	/** Get the import action value. This is the value to use for serialization. */
	@JsonValue	
	public String getValue() {
		return value
	}
}
