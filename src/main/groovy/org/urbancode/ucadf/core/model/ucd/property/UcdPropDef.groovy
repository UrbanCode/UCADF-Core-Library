/**
 * This class is the super class of property sheet definition objects.
 */
package org.urbancode.ucadf.core.model.ucd.property

import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.As

import groovy.util.logging.Slf4j

@Slf4j
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME, 
	include = As.EXISTING_PROPERTY, 
	property = "type", 
	visible=true
)
@JsonSubTypes([
	@Type(value = UcdPropDefCheckBox.class, name = UcdPropDef.TYPE_CHECKBOX),
	@Type(value = UcdPropDefDateTime.class, name = UcdPropDef.TYPE_DATETIME),
	@Type(value = UcdPropDefHttpMultiSelect.class, name = UcdPropDef.TYPE_HTTP_MULTI_SELECT),
	@Type(value = UcdPropDefHttpSelect.class, name = UcdPropDef.TYPE_HTTP_SELECT),
	@Type(value = UcdPropDefMultiSelect.class, name = UcdPropDef.TYPE_MULTI_SELECT),
	@Type(value = UcdPropDefSecure.class, name = UcdPropDef.TYPE_SECURE),
	@Type(value = UcdPropDefSelect.class, name = UcdPropDef.TYPE_SELECT),
	@Type(value = UcdPropDefText.class, name = UcdPropDef.TYPE_TEXT),
	@Type(value = UcdPropDefTextArea.class, name = UcdPropDef.TYPE_TEXTAREA)
])
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class UcdPropDef extends UcdObject {
	public final static String TYPE_CHECKBOX = "CHECKBOX"
	public final static String TYPE_DATETIME = "DATETIME"
	public final static String TYPE_HTTP_MULTI_SELECT = "HTTP_MULTI_SELECT"
	public final static String TYPE_HTTP_SELECT = "HTTP_SELECT"
	public final static String TYPE_MULTI_SELECT = "MULTI_SELECT"
	public final static String TYPE_SECURE = "SECURE"
	public final static String TYPE_SELECT = "SELECT"
	public final static String TYPE_TEXT = "TEXT"
	public final static String TYPE_TEXTAREA = "TEXTAREA"
	
	/** The property definition ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The label. */	
	String label
	
	/** The type. */
	String type
	
	/** The pattern. */
	String pattern
	
	/** The flag that indicates required. */
	Boolean required
	
	/** TODO: What's this? */
	String placeholder
	
	/** The value. For MULTI_SELECT this is a comma-delimited value. */
	String value = ""
	
	/** The flag that indicates inherited. */
	Boolean inherited

	/** The index. */
	Long index
	
	/**
	 * A workaround to fix HTTP property definitions exported from an older version and imported to a new version.
	 * @param application The application name or ID.
	 * @param process The process name or ID.
	 * @param ucdPropDefs The list of property definitions.
	 */
	public static fixHttpPropDefs(
		final String application, 
		final String process, 
		final List<UcdPropDef> ucdPropDefs,
		final UcdSession ucdSession) {
		
		for (int i = 0; i < ucdPropDefs.size(); i++) {
			if (ucdPropDefs[i].getType() == UcdPropDef.TYPE_HTTP_SELECT || ucdPropDefs[i].getType() == UcdPropDef.TYPE_HTTP_MULTI_SELECT) {
				UcdPropDefHttpSelect propDef = ucdPropDefs[i]
				
				if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_61)) {
					// Temporarily replace the HTTP property definition with a SELECT property definition of the same name.
					log.info "Replacing HTTP property definition for application [$application] process [$process] property [${propDef.getName()}] httpUrl [${((UcdPropDefHttpSelect)propDef).getHttpUrl()}]."
					
					UcdPropDefSelect newPropDef = new UcdPropDefSelect()
					newPropDef.setName(propDef.getName())
					ucdPropDefs[i] = newPropDef
				} else if (ucdSession.compareVersion(UcdSession.UCDVERSION_70) >= 0) {
					// Fix the HTTP authentication type value.
					if (!propDef.getHttpAuthenticationType()) {
						log.info "Fixing HTTP property definition for application [$application] process [$process] property [${propDef.getName()}] httpUrl [${propDef.getHttpUrl()}]."
						propDef.setHttpAuthenticationType("BASIC")
					}
				}
			}
		}
	}
	
	/**
	 * Derive a request request map.
	 * @param ucdProcess
	 * @param replaceUcdPropDef
	 * @return
	 */
	public Map deriveRequestMap(
		final Object ucdProcess,
		final UcdPropDef ucdPropDef = null) {
		
		// If no property definition was provided then this is an add so use this.
		UcdPropDef replaceUcdPropDef = ucdPropDef
		if (!replaceUcdPropDef) {
			replaceUcdPropDef = this
		}
		
        Map requestMap = [
			name: replaceUcdPropDef.getName(), 
			description: (replaceUcdPropDef.getDescription() != null) ? replaceUcdPropDef.getDescription() : description,
			label: (replaceUcdPropDef.getLabel() != null) ? replaceUcdPropDef.getLabel() : label,
			type: (replaceUcdPropDef.getType() != null) ? replaceUcdPropDef.getType() : type,
			pattern: (replaceUcdPropDef.getPattern() != null) ? replaceUcdPropDef.getPattern() : pattern,
			required: (replaceUcdPropDef.getRequired() != null) ? replaceUcdPropDef.getRequired() : required,
			value: (replaceUcdPropDef.getValue() != null) ? replaceUcdPropDef.getValue() : value
		]
		
		// If and ID is defined then this must be an update.
		if (id) {
			requestMap.put('existingId', id)
		}
		
		// Values for Application process.
		if (ucdProcess instanceof UcdApplicationProcess) {
			requestMap.put('applicationProcessVersion', ucdProcess.getVersionCount())
		}
		
		// Values for Component process.
		if (ucdProcess instanceof UcdComponentProcess) {
			requestMap.put('componentProcessVersion', ucdProcess.getVersionCount())
		}
		
		// Values for Generic process.
		if (ucdProcess instanceof UcdGenericProcess) {
			requestMap.put('definitionGroupId', ucdProcess.getPropSheetDef().getId())
			requestMap.put('processVersion', ucdProcess.getVersionCount())
		}
		
		return requestMap
	}
}
