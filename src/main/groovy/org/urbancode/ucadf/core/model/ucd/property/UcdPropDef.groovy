/**
 * This class is the super class of property sheet definition objects.
 */
package org.urbancode.ucadf.core.model.ucd.property

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.As

import groovy.util.logging.Slf4j

@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.EXISTING_PROPERTY, property = "type", visible=true)
@JsonSubTypes([
	@Type(value = UcdPropDefCheckBox.class, name = UcdPropDef.TYPE_CHECKBOX),
	@Type(value = UcdPropDefHttpMultiSelect.class, name = UcdPropDef.TYPE_HTTP_MULTI_SELECT),
	@Type(value = UcdPropDefHttpSelect.class, name = UcdPropDef.TYPE_HTTP_SELECT),
	@Type(value = UcdPropDefMultiSelect.class, name = UcdPropDef.TYPE_MULTI_SELECT),
	@Type(value = UcdPropDefSecure.class, name = UcdPropDef.TYPE_SECURE),
	@Type(value = UcdPropDefSelect.class, name = UcdPropDef.TYPE_SELECT),
	@Type(value = UcdPropDefText.class, name = UcdPropDef.TYPE_TEXT),
	@Type(value = UcdPropDefTextArea.class, name = UcdPropDef.TYPE_TEXTAREA)
])
abstract class UcdPropDef extends UcdObject {
	public final static String TYPE_CHECKBOX = "CHECKBOX"
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
	
	/** The value. */
	String value = ""
	
	/** The flag that indicates inherited. */
	Boolean inherited

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
				} else if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_70)) {
					// Fix the HTTP authentication type value.
					if (!propDef.getHttpAuthenticationType()) {
						log.info "Fixing HTTP property definition for application [$application] process [$process] property [${propDef.getName()}] httpUrl [${propDef.getHttpUrl()}]."
						propDef.setHttpAuthenticationType("BASIC")
					}
				}
			}
		}
	}
}
