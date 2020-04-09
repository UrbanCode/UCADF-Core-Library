/**
 * This class is used to import application templates.
 */
package org.urbancode.ucadf.core.model.ucd.applicationTemplate

import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessImport
import org.urbancode.ucadf.core.model.ucd.environmentTemplate.UcdEnvironmentTemplate
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcessImport
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImport
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.status.UcdStatus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationTemplateImport extends UcdImport {
	/** The name. */
	String name
	
	/** The description. */
	String description
	
	/** The application processes to import. */
	List<UcdApplicationProcessImport> applicationProcessList
	
	/** The generic processes to import. */
	List<UcdGenericProcessImport> genericProcesses
	
	/** The list of tag requirements. */	
	List<UcdApplicationTemplateTagRequirement> tagRequirements
	
	/** The flag to enforce complete snapshots. */
	Boolean enforceCompleteSnapshots
	
	/** The list of environment templates. */	
	List<UcdEnvironmentTemplate> environmentTemplates
	
	/** The property definitions. */	
	List<UcdPropDef> propDefs
	
	/** The list of statuses. */
	List<UcdStatus> statuses
	
	// Constructors.
	UcdApplicationTemplateImport() {
	}
}
