/**
 * This action exports an application.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucadf.general.UcAdfUpdatePropertiesFile
import org.urbancode.ucadf.core.action.ucd.applicationProcess.UcdGetApplicationProcesses
import org.urbancode.ucadf.core.action.ucd.applicationProcess.UcdUpdateApplicationProcess
import org.urbancode.ucadf.core.action.ucd.role.UcdGetRoles
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.application.UcdApplicationImport
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.importExport.UcdExport
import org.urbancode.ucadf.core.model.ucd.role.UcdRole
import org.urbancode.ucadf.core.model.ucd.role.UcdRolesMap

import com.fasterxml.jackson.databind.ObjectMapper

class UcdExportApplication extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** (Optional) The file name to which to write the exported application. */
	String fileName = ""
	
	/**
	 * Runs the action.
	 * @return The export object.
	 */
	public UcdExport run() {
		// Validate the action properties.
		validatePropsExist()

		logVerbose("\n=== Exporting application [$application] from [${ucdSession.getUcdUrl()}] ===")

		// Get the actions to set the application process required roles after the import.
		List<UcdRole> ucdRoles = actionsRunner.runAction([
			action: UcdGetRoles.getSimpleName()
		])

		// Convert the roles list to a ID map.
		UcdRolesMap rolesIdMap = UcdRolesMap.getRolesIdMap(
			ucdRoles
		)

		List<UcdApplicationProcess> ucdApplicationProcesses = actionsRunner.runAction([
			action: UcdGetApplicationProcesses.getSimpleName(),
			application: application,
			full: false
		])

		// Write actions to update the process required role. This is to work around an export/import API bug that was losing the required role.
		List<Map> actions = []
		for (ucdApplicationProcess in ucdApplicationProcesses) {
			if (ucdApplicationProcess.getRequiredRoleId()) {
				// Need this to create an action with a role name instead of a role ID.
				UcdRole role = rolesIdMap.get(ucdApplicationProcess.getRequiredRoleId())
				
				actions.add(
					[
						action: UcdUpdateApplicationProcess.getSimpleName(),
						application: ucdApplicationProcess.getApplication().getName(),
						process: ucdApplicationProcess.getName(),
						requiredRole: role.getName()
					]
				)
			}
		}

		UcdApplicationImport ucdApplicationImport
		
		// Get the application export text.
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/application/{application}/export")
			.resolveTemplate("application", application)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdApplicationImport = response.readEntity(UcdApplicationImport.class)
		} else {
			throw new UcAdfInvalidValueException(response)
		}

		// The export object to return.
		UcdExport ucdExport = new UcdExport(ucdApplicationImport)
		
		// Optionally write the export files.
		if (fileName) {
			// Initialize the export directory.
			File exportFile = new File(fileName)
			exportFile.getParentFile()?.mkdirs()
			
			// Write the supplemental actions file.
			String actionsFileName = fileName.replaceAll(/\..*?$/, "") + ".actions.json"
			File actionsFile = new File(actionsFileName)

			logVerbose("Saving application [$application] export actions to file [$actionsFileName].")
			ObjectMapper mapper = new ObjectMapper()
			actionsFile.write(
				mapper.writer().withDefaultPrettyPrinter().writeValueAsString(
					[ actions: actions ]
				)
			)
		
			logVerbose("Saving application [$application] export to file [$fileName].")
			exportFile.write(ucdApplicationImport.toJsonString())
		
			// Write the keystore names and UCD version to the application properties file.		
			String propertiesFileName = fileName.replaceAll(/\..*?$/, "") + ".properties"
			
			logVerbose("Saving application [$application] export properties to file [$propertiesFileName].")
			actionsRunner.runAction([
				action: UcAdfUpdatePropertiesFile.getSimpleName(),
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
