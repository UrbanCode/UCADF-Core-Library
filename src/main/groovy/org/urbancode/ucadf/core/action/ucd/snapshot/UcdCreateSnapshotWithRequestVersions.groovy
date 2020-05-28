/**
 * This action creates a snapshot of the requested versions.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import org.urbancode.ucadf.core.action.ucd.applicationProcessRequest.UcdGetApplicationProcessRequestVersions
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcdCreateSnapshotWithRequestVersions extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	String requestId
	
	/** The snapshot name. */
	String name
	
	/** (Optional) The description. */
	String description = ""
	
	/** This flag indicates to validate that only one full version of each component is selected. */
	Boolean validateSingleFullVersions = true
	
	/** This flag indicates to require versions. */
	Boolean requireVersions = true
	
	/** The flag that indicates fail if the snapshot already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the snapshot was created.
	 */
	@Override
	public Boolean run() {
		validatePropsExist()

		Boolean created = false
		
		logVerbose("Creating application [$application] snapshot [$name] with versions from application process request [$requestId].")
		
		// Get the versions from the application process request.
		List<Map<String, String>> requestVersions = actionsRunner.runAction([
			action: UcdGetApplicationProcessRequestVersions.getSimpleName(),
			actionInfo: actionInfo,
			actionVerbose: actionVerbose,
			requestId: requestId,
			validateSingleFullVersions: validateSingleFullVersions,
			returnAs: UcdGetApplicationProcessRequestVersions.ReturnAsEnum.LISTMAP
		])

		if (requireVersions && requestVersions.size() < 1) {
			throw new UcAdfInvalidValueException("This snapshot must be created with at least one component version selected.")
		}
		
		// Create the snapshot with the versions.
		created = actionsRunner.runAction([
			action: UcdCreateSnapshot.getSimpleName(),
			actionInfo: actionInfo,
			actionVerbose: actionVerbose,
			application: application,
			name: name,
			description: description,
			versions: requestVersions,
			failIfExists: failIfExists
		])
		
		return created
	}	
}
