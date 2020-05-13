/**
 * This action gets the next available sequential snapshot number.
 */
package org.urbancode.ucadf.core.action.ucd.snapshot

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucadf.general.UcAdfGetNextVersionNumber
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterField
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldTypeEnum
import org.urbancode.ucadf.core.model.ucd.snapshot.UcdSnapshot

class UcdGetNextSnapshotVersionNumber extends UcAdfAction {
	// Action properties.
	/** The application name or ID. */
	String application

	/** The version pattern, e.g.  *, 1.*, 1.0.*, etc. */
	String versionPattern
	
	/**
	 * Runs the action.	
	 * @return The next available sequential snapshot number as a string.
	 */
	@Override
	public String run() {
		// Validate the action properties.
		validatePropsExist()
		
		String nextSnapshotVersionNumber = versionPattern
		
		if (versionPattern ==~ /.*\*$/) {
			logVerbose("Getting next snapshot version for application [$application] version [$versionPattern].")
			
			List<UcdSnapshot> snapshots = []
			
			// Construct the query fields.
			List<UcdFilterField> filterFields = [
				new UcdFilterField(
					"name",
					versionPattern,
					UcdFilterFieldTypeEnum.like
				)
			]

			WebTarget target = UcdFilterField.addFilterFieldQueryParams(
				ucdSession.getUcdWebTarget().path("/rest/deploy/application/{application}/snapshots/active").resolveTemplate("application", application),
				filterFields
			)
	
			Response response = target.request().get()
			if (response.getStatus() != 200) {
				throw new UcAdfInvalidValueException(response)
			}
			
			snapshots = response.readEntity(new GenericType<List<UcdSnapshot>>(){})
			
			List<String> versions = []
			for (snapshot in snapshots) {
				versions.add(snapshot.getName())
			}
			
			nextSnapshotVersionNumber = actionsRunner.runAction([
				action: UcAdfGetNextVersionNumber.getSimpleName(),
				actionInfo: false,
				actionVerbose: actionVerbose,
				versionPattern: versionPattern,
				versions: versions
			])

			logVerbose("Next snapshot version [$nextSnapshotVersionNumber].")
		}
		
		return nextSnapshotVersionNumber
	}
}
