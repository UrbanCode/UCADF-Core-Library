/**
 * This class instantiates HTTP select property definition objects.
 */
package org.urbancode.ucadf.core.model.ucd.property

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdPropDefHttpSelect extends UcdPropDef {
	/** The HTTP URL. */
	String httpUrl
	
	/** The user name. */
	String httpUsername
	
	/** The user password. *?
	String httpPassword
	
	/** The user encrypted password. */
	String httpEncryptedPassword
	
	/* The HTTP format. */
	String httpFormat
	
	/* The HTTP base batah. */
	String httpBasePath
	
	/* The HTTP value path. */
	String httpValuePath
	
	/* The HTTP label path. */
	String httpLabelPath
	
	/* The HTTP resolve valur URL. */
	String resolveHttpValuesUrl
	
	/* The HTTP authentication type. */
	String httpAuthenticationType
	
	UcdPropDefHttpSelect() {
		this.type = TYPE_HTTP_SELECT
	}
}
