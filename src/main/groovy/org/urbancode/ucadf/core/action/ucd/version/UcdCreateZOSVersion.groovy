/**
 * This action creates a zOS version.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionTypeEnum

class UcdCreateZOSVersion extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component

	/** The version name. */	
	String name
	
	/** The version type. Default is INCREMENTAL. */
	UcdVersionTypeEnum type = UcdVersionTypeEnum.INCREMENTAL
	
	/** Type of repository e.g. ('CODESTATION' or 'HFS'). (Optional) */
	String repositoryType = "CODESTATION"
	
	/** The Package Manifest file (e.g. packageManifest.xml). */
	File file
	
	/** The flag that indicates fail if the version already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the version was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		if (!file.exists()) {
			throw new UcAdfInvalidValueException("File [$file] not found.")
		}
		
		// Get the existing version information.
		UcdVersion ucdVersion = actionsRunner.runAction([
			action: UcdGetVersion.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			component: component,
			version: name,
			failIfNotFound: false
		])

		// If the version doesn't exist then create it.
		if (ucdVersion) {
			logVerbose("Component [$component] version [$name] already exists.")
			if (failIfExists) {
				throw new UcAdfInvalidValueException("Component [$component] version [$name] already exists.")
			}
		} else {
			logVerbose("Creating zOS version component [$component] version [$name].")

			WebTarget target = ucdSession.getUcdWebTarget().path("/cli/version/createZOSVersion")
				.queryParam("component", component)
				.queryParam("version", name)
				.queryParam("type", type)
				.queryParam("repositoryType", repositoryType)
			logDebug("target=$target")

			Response response = target.request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(Entity.json(file.text))
			if (response.getStatus() == 200) {
				ucdVersion = response.readEntity(UcdVersion.class)
				created = true
			} else {
				throw new UcAdfInvalidValueException(response)
			}
			
			logVerbose("Component [$component] zOS version [$name] id [${ucdVersion.getId()}] created.")
			
			return created
		}
	}
}
