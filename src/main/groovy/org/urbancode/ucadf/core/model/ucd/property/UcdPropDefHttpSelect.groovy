/**
 * This class instantiates HTTP select property definition objects.
 */
package org.urbancode.ucadf.core.model.ucd.property

import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdPropDefHttpSelect extends UcdPropDef {
	/** The HTTP URL. */
	String httpUrl
	
	/** The user name. */
	String httpUsername
	
	/** The user password. */
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
	
	/* The flag to indicate to use bearer authentication */
	Boolean httpUseBearerAuth
	
	UcdPropDefHttpSelect() {
		this.type = TYPE_HTTP_SELECT
	}

	/**
	 * Derive an update request map.
	 * @param ucdProcess
	 * @param replaceUcdPropDef
	 * @return
	 */
	@Override
	public Map deriveRequestMap(
		final Object ucdProcess,
		final UcdPropDef ucdPropDef = null) {

		// If no property definition was provided then this is an add so use this.
		UcdPropDefHttpSelect replaceUcdPropDef = ucdPropDef
		if (!replaceUcdPropDef) {
			replaceUcdPropDef = this
		}

		// Derive the common property definition map.
        Map requestMap = super.deriveRequestMap(
			ucdProcess,
			replaceUcdPropDef
		)

		requestMap.put('httpUrl', (replaceUcdPropDef.getHttpUrl() != null) ? replaceUcdPropDef.getHttpUrl() : httpUrl)
		requestMap.put('httpAuthenticationType', (replaceUcdPropDef.getHttpAuthenticationType() != null) ? replaceUcdPropDef.getHttpAuthenticationType() : httpAuthenticationType)
		requestMap.put('httpUseBearerAuth', (replaceUcdPropDef.getHttpUseBearerAuth() != null) ? replaceUcdPropDef.getHttpUseBearerAuth() : httpUseBearerAuth)
		requestMap.put('httpUsername', (replaceUcdPropDef.getHttpUsername() != null) ? replaceUcdPropDef.getHttpUsername() : httpUsername)
		requestMap.put('httpPassword', (replaceUcdPropDef.getHttpPassword() != null) ? replaceUcdPropDef.getHttpPassword().toString() : httpPassword)
		requestMap.put('httpFormat', (replaceUcdPropDef.getHttpFormat() != null) ? replaceUcdPropDef.getHttpFormat() : httpFormat)
		requestMap.put('httpBasePath', (replaceUcdPropDef.getHttpBasePath() != null) ? replaceUcdPropDef.getHttpBasePath() : httpBasePath)
		requestMap.put('httpValuePath', (replaceUcdPropDef.getHttpValuePath() != null) ? replaceUcdPropDef.getHttpValuePath() : httpValuePath)
		requestMap.put('httpLabelPath', (replaceUcdPropDef.getHttpLabelPath() != null) ? replaceUcdPropDef.getHttpLabelPath() : httpLabelPath)

		// Values for Application process.
		if (ucdProcess instanceof UcdApplicationProcess) {
			requestMap.put('resolveHttpValueUrl', "rest/deploy/applicationProcess/${ucdProcess.getId()}/propDefs/resolveHttpValues/$name")
		}

		// Values for Component process.
		if (ucdProcess instanceof UcdComponentProcess) {
			requestMap.put('resolveHttpValueUrl', "rest/deploy/componentProcess/${ucdProcess.getId()}/propDefs/resolveHttpValues/$name")
		}

		// Values for Generic process.	
		if (ucdProcess instanceof UcdGenericProcess) {
			requestMap.put('resolveHttpValueUrl', "rest/process/${ucdProcess.getId()}/propDefs/resolveHttpValues/$name")
		}
		
		return requestMap
	}
}
