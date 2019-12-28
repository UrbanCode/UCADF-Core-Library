/**
 * This action exports a component template.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplateImport
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.importExport.UcdExport

class UcdExportComponentTemplate extends UcAdfAction {
	// Action properties.
	/** The component template name or ID. */
	String componentTemplate
	
	/** The file name. */
	String fileName = ""
	
	/**
	 * Runs the action.	
	 * @return The component template export object.
	 */
	@Override
	public UcdExport run() {
		// Validate the action properties.
		validatePropsExist()

		UcdComponentTemplateImport ucdComponentTemplateImport = new UcdComponentTemplateImport()
				
		// If a component template ID was provided then use it. Otherwise get the component template information to get the ID.
		String componentTemplateId = componentTemplate
		if (!UcdObject.isUUID(componentTemplate)) {
			UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
				action: UcdGetComponentTemplate.getSimpleName(),
				componentTemplate: componentTemplate,
				failIfNotFound: true
			])
			componentTemplateId = ucdComponentTemplate.getId()
		}
		
		logInfo("\n=== Exporting Component template [$componentTemplate] from [${ucdSession.getUcdUrl()}] ===")

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentTemplate/{componentTemplateId}")
			.resolveTemplate("componentTemplateId", componentTemplateId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentTemplateImport = response.readEntity(UcdComponentTemplateImport.class)
		} else {
			throw new UcdInvalidValueException(response)
		}
		
		// Optionally save to file.
		if (fileName) {
			logInfo("Saving export to file [$fileName].")
			
			File exportFile = new File(fileName)
			
			// Create the file's target directory.
			exportFile.getParentFile()?.mkdirs()
			
			// Write the export file.
			exportFile.write(ucdComponentTemplateImport.toJsonString())
		}
		
		return new UcdExport(ucdComponentTemplateImport)
	}
}
