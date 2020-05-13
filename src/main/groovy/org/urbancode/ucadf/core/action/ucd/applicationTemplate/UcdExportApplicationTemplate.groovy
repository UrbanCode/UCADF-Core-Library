/**
 * This action exports a application template.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplate

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucadf.general.UcAdfUpdatePropertiesFile
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplateImport
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.importExport.UcdExport

class UcdExportApplicationTemplate extends UcAdfAction {
	// Action properties.
	/** The application template name or ID. */
	String applicationTemplate
	
	/** The file name. */
	String fileName = ""
	
	/**
	 * Runs the action.	
	 * @return The application template export object.
	 */
	@Override
	public UcdExport run() {
		// Validate the action properties.
		validatePropsExist()

		UcdApplicationTemplateImport ucdApplicationTemplateImport = new UcdApplicationTemplateImport()
				
		// If a application template ID was provided then use it. Otherwise get the application template information to get the ID.
		String applicationTemplateId = applicationTemplate
		if (!UcdObject.isUUID(applicationTemplate)) {
			UcdApplicationTemplate ucdApplicationTemplate = actionsRunner.runAction([
				action: UcdGetApplicationTemplate.getSimpleName(),
				actionInfo: false,
				applicationTemplate: applicationTemplate,
				failIfNotFound: true
			])
			applicationTemplateId = ucdApplicationTemplate.getId()
		}
		
		logVerbose("\n=== Exporting Application template [$applicationTemplate] from [${ucdSession.getUcdUrl()}] ===")

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate/{applicationTemplateId}/export")
			.resolveTemplate("applicationTemplateId", applicationTemplateId)
		logVerbose("target=$target")

		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationTemplateImport = response.readEntity(UcdApplicationTemplateImport.class)
		} else {
			throw new UcAdfInvalidValueException(response)
		}
		
		// The export object to return.
		UcdExport ucdExport = new UcdExport(ucdApplicationTemplateImport)

		// Optionally save to file.
		if (fileName) {
			logVerbose("Saving export to file [$fileName].")
			
			File exportFile = new File(fileName)
			
			// Create the file's target directory.
			exportFile.getParentFile()?.mkdirs()
			
			// Write the export file.
			exportFile.write(ucdApplicationTemplateImport.toJsonString())
			
			// Write the keystore names and UCD version to the application template properties file.		
			String propertiesFileName = fileName.replaceAll(/\..*?$/, "") + ".properties"
			
			logVerbose("Saving application template [$applicationTemplate] export properties to file [$propertiesFileName].")
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
