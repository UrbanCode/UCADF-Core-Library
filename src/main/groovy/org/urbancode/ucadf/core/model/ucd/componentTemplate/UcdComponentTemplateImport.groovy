/**
 * This class is used to import component templates.
 */
package org.urbancode.ucadf.core.model.ucd.componentTemplate

import org.urbancode.ucadf.core.model.ucd.component.UcdComponentTypeEnum
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcessImport
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcessImport
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImport
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentTemplateImport extends UcdImport {
	/** The path. */
	String path
	
	/** The component type. */
	UcdComponentTypeEnum componentType
	
	/** The flag that indicates ignore qualifiers. */
	Long ignoreQualifiers
	
	/** The flag that indicates the component template is active. */
	Boolean active
	
	/** The tags. */
	List<UcdTag> tags
	
	/** The property definitions. */
	List<UcdPropDef> propDefs

	/** The environment property definitions. */
	List<UcdPropDef> envPropDefs
	
	/** The resource property definitions. */
	List<UcdPropDef> resPropDefs

	/** The properties. */
	List<UcdProperty> properties
	
	/** The component processes. */
	List<UcdComponentProcessImport> processes
	
	/** The generic processes. */
	List<UcdGenericProcessImport> genericProcesses
	
	/** The security resource ID. */
	String securityResourceId
}
