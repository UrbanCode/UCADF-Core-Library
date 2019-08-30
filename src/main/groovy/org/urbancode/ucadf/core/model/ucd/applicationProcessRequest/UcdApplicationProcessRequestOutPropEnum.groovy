/**
 * This enumeration represents the application process request outProp property value names.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcessRequest

import com.fasterxml.jackson.annotation.JsonValue

enum UcdApplicationProcessRequestOutPropEnum {
	REQUESTID("requestId"),
	RESPONSERESULT("responseResult"),
	RESPONSESTATUS("responseStatus"),
	PROCESSSTATUS("UcAdfPluginStatus"),
	HTTPSTATUS("httpStatus"),
	DETAILSLINK("detailsLink")

	private String propertyName
	
	// Constructor.	
	UcdApplicationProcessRequestOutPropEnum(final String propertyName) {
		this.propertyName = propertyName
	}

	public String getPropertyName() {
		return propertyName
	}
}
