/**
 * This action exports a component.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentImport
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.importExport.UcdExport

class UcdExportComponent extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** (Optional) The file to which to export the component. */	
	String fileName = ""
	
	/**
	 * Runs the action.	
	 * @return The export object.
	 */
	@Override
	public UcdExport run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("\n=== Exporting Component [$component] from [${ucdSession.getUcdUrl()}] ===")

		UcdComponentImport ucdComponentImport = new UcdComponentImport()
				
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component/{component}/export")
			.resolveTemplate("component", component)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdComponentImport = response.readEntity(UcdComponentImport.class)
		} else {
			throw new UcdInvalidValueException(response)
		}
		
		// Optionally save to file.
		if (fileName) {
			logVerbose("Saving component [$component] export to file [$fileName].")
			
			File exportFile = new File(fileName)
			
			// Create the file's target directory.
			exportFile.getParentFile()?.mkdirs()
			
			// Write the export file.
			exportFile.write(ucdComponentImport.toJsonString())
		}
		
		return new UcdExport(ucdComponentImport)
	}
}
