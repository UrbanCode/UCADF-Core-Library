/**
 * This class is used for component import.
 */
package org.urbancode.ucadf.core.model.ucd.component

import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcessImport
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplateImport
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcessImport
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImport
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionTypeEnum

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentImport extends UcdImport {
	/** The list of component processes. */
	List<UcdComponentProcessImport> processes
	
	/** The list of generic processes. */
	List<UcdGenericProcessImport> genericProcesses
	
	/** The date created. */
	Long created
	
	/** The component type. */
	UcdComponentTypeEnum componentType
	
	/** The flag that indicates ignore qualifiers. */
	Long ignoreQualifiers
	
	/** The flag that indicates import automatically. */
	Boolean importAutomatically
	
	/** The flag that indicates use VFS. */
	Boolean useVfs
	
	/** The flag that indicates the component is active. */
	Boolean active
	
	/** The flag that indicates the component is deleted. */
	Boolean deleted
	
	/** The default version type. */
	UcdVersionTypeEnum defaultVersionType

	/** The number of cleanup days to keep. */	
	Long cleanupDaysToKeep
	
	/** The cleanup count to keep. */
	Long cleanupCountToKeep
	
	/** The source configuration plugin name. */
	String sourceConfigPluginName
	
	/** The list of environment property definitions. */
	List<UcdPropDef> envPropDefs
	
	/** The list of resource property definitions. */
	List<UcdPropDef> resPropDefs
	
	/** The list of version property definitions. */
	List<UcdPropDef> versionPropDefs
	
	/** The property sheet group. TODO: Need to add class. */
	Map propSheetGroup
	
	/** The list of configuration templates. TODO: Need to add class. */
	List configTemplates
	
	/** The component template. */
	UcdComponentTemplateImport componentTemplate
	
	/** The tags on the compnent. */
	List<UcdTag> tags
	
	// Constructors.	
	UcdComponentImport() {
	}
	
	public Map<String, UcdGenericProcessImport> getGenericProcessImports(final String match = "") {
		return getGenericProcessImports(
			genericProcesses, 
			match
		)
	}
}
