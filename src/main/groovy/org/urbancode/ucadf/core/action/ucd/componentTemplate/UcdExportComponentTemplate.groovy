/**
 * This action exports a component template.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucadf.general.UcAdfUpdatePropertiesFile
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplateImport
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
				actionInfo: false,
				actionVerbose: false,
				componentTemplate: componentTemplate,
				failIfNotFound: true
			])
			componentTemplateId = ucdComponentTemplate.getId()
		}
		
		logVerbose("\n=== Exporting Component template [$componentTemplate] from [${ucdSession.getUcdUrl()}] ===")

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentTemplate/{componentTemplateId}")
			.resolveTemplate("componentTemplateId", componentTemplateId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentTemplateImport = response.readEntity(UcdComponentTemplateImport.class)
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		// The export object to return.
		UcdExport ucdExport = new UcdExport(ucdComponentTemplateImport)

		// Optionally write the export files.
		if (fileName) {
			logVerbose("Saving export to file [$fileName].")
			
			// Create the file's target directory.
			File exportFile = new File(fileName)
			exportFile.getParentFile()?.mkdirs()
			
			// Write the export file.
			exportFile.write(ucdComponentTemplateImport.toJsonString())
		
			// Write the keystore names and UCD version to the component template properties file.		
			String propertiesFileName = fileName.replaceAll(/\..*?$/, "") + ".properties"
			
			logVerbose("Saving component template [$componentTemplate] export properties to file [$propertiesFileName].")
			actionsRunner.runAction([
				action: UcAdfUpdatePropertiesFile.getSimpleName(),
				actionInfo: false,
				fileName: propertiesFileName,
			    propertyValues: [
					keystoreNames: ucdExport.getKeystoreNames().join(","),
					ucdVersion: ucdSession.getUcdVersion()
				]
			])
		}

		return ucdExport
	}
}
