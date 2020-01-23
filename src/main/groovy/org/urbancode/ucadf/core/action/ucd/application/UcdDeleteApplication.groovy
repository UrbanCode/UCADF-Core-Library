/**
 * This action deletes an application.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.snapshot.UcdDeleteSnapshot
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

class UcdDeleteApplication extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application

	/** The flag that indicates the application's snapshots should be deleted first so that the application can be deleted. Default is true. */	
	Boolean deleteSnapshots = true
	
	/** The flag that indicates fail if the application is not found. Default is false. */
	Boolean failIfNotFound = false
	
	/** The flag that indicates to perform the delete, otherwise show that the delete would be done. Default is true. */
	Boolean commit = true
	
	/**
	 * Runs the action.
	 * @return True if the application was deleted.
	 */
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean deleted = false
				
		logVerbose("Delete application [$application] commit [$commit].")

		UcdApplication ucdApplication = actionsRunner.runAction([
			action: UcdGetApplication.getSimpleName(),
			actionInfo: false,
			application: application,
			failIfNotFound: failIfNotFound
		])

		if (ucdApplication) {
			if (commit) {
	            // Delete the application snapshots. Added for UCD 6.2 that requires this.
				if (deleteSnapshots) {
					List<UcdSnapshot> ucdSnapshots = actionsRunner.runAction([
						action: UcdGetSnapshotsInApplication.getSimpleName(),
						actionInfo: false,
						application: application
					])
					
					for (ucdSnapshot in ucdSnapshots) {
						actionsRunner.runAction([
							action: UcdDeleteSnapshot.getSimpleName(),
							actionInfo: false,
							actionVerbose: actionVerbose,
							application: application,
							snapshot: ucdSnapshot.getName()
						])
					}
				}
				
	            // Delete the application
				WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/application/{application}")
					.resolveTemplate("application", application)
				logDebug("target=$target")
				
				Response response = target.request(MediaType.APPLICATION_JSON).delete()
				if (response.getStatus() == 204) {
					logVerbose("Application [$application] deleted.")
					deleted = true
				} else {
					throw new UcdInvalidValueException(response)
				}
			} else {
				logVerbose("Would delete application [$application].")
			}
		}
		
		return deleted
	}
}
