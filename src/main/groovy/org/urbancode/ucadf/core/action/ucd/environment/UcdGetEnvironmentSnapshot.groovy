/**
 * This action gets the snapshot currently associated with the environment.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

class UcdGetEnvironmentSnapshot extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application
	
	/** The environment name or ID. */
	String environment
	
	/**
	 * Runs the action.	
	 * @return The snapshot object.
	 */
	@Override
	public UcdSnapshot run() {
		// Validate the action properties.
		validatePropsExist()
	
		// Get the snapshot name currently associated with an environment
		UcdEnvironment ucdEnvironment = actionsRunner.runAction([
			action: UcdGetEnvironment.getSimpleName(),
			application: application,
			environment: environment,
			withDetails: true,
			failIfNotFound: true
		])

		return ucdEnvironment.getLatestSnapshot()
	}
}
