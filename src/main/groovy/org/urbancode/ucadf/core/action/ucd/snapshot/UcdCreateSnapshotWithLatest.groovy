/**
 * This action creates a snapshot with the latest available component versions.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import org.urbancode.ucadf.core.action.ucd.application.UcdGetComponentsInApplication
import org.urbancode.ucadf.core.action.ucd.version.UcdGetComponentLatestVersion
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdCreateSnapshotWithLatest extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The snapshot name. */
	String name
	
	/** (Optional) The description. */
	String description = ""
	
	/** The flag that indicates fail if the snapshot already exists. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return True if the snapshot was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		// Get the latest versions of the application's components.
		List<Map<String, String>> latestVersions = []
		
		List<UcdComponent> ucdComponents = actionsRunner.runAction([
			action: UcdGetComponentsInApplication.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			application: application
		])

		for (ucdComponent in ucdComponents) {
			logVerbose("Getting latest version of component ${ucdComponent.getName()}.")
			
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetComponentLatestVersion.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: ucdComponent.getName(),
				failIfNotFound: false
			])
			
			if (ucdVersion) {
				logVerbose("Latest version of component ${ucdComponent.getName()} is ${ucdVersion.getName()}.")
				latestVersions.add(
					[
						(ucdComponent.getName()): ucdVersion.getName()
					]
				)
			}
		}
		
		// Create the snapshot with the versions.
		created = actionsRunner.runAction([
			action: UcdCreateSnapshot.getSimpleName(),
			actionInfo: false,
			actionVerbose: actionVerbose,
			application: application,
			name: name,
			description: description,
			versions: latestVersions,
			failIfExists: failIfExists
		])
		
		return created
	}	
}
