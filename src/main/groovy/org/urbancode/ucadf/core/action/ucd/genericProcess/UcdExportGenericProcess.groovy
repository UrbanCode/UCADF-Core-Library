/**
 * This action exports a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcessImport
import org.urbancode.ucadf.core.model.ucd.importExport.UcdExport

class UcdExportGenericProcess extends UcAdfAction {
	// Action properties.
	/** The generic process name or ID. */
	String process

	/** The file name. */	
	String fileName = ""
	
	/**
	 * Runs the action.	
	 * @return The export object.
	 */
	@Override
	public UcdExport run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("\n=== Exporting generic process [$process] from [${ucdSession.getUcdUrl()}] ===")

		// If an generic process ID was provided then use it. Otherwise get the generic process information to get the ID.
		String processId = process
		if (!UcdObject.isUUID(process)) {
			UcdGenericProcess ucdGenericProcess = actionsRunner.runAction([
				action: UcdGetGenericProcess.getSimpleName(),
				process: process,
				failIfNotFound: true
			])
			
			processId = ucdGenericProcess.getId()
		}
		
		UcdGenericProcessImport ucdGenericProcessImport
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/{processId}/export")
			.resolveTemplate("processId", processId)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdGenericProcessImport = response.readEntity(UcdGenericProcessImport.class)
		} else {
			throw new UcdInvalidValueException(response)
		}
		
		// Optionally save to file.
		if (fileName) {
			logVerbose("Saving export to file [$fileName].")
			
			File exportFile = new File(fileName)
			
			// Create the file's target directory.
			exportFile.getParentFile()?.mkdirs()
			
			// Write the export file.
			exportFile.write(ucdGenericProcessImport.toJsonString())
		}
		
		return new UcdExport(ucdGenericProcessImport)
	}
}
