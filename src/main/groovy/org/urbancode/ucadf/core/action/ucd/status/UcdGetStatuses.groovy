/**
 * This action get a status.
 */
package org.urbancode.ucadf.core.action.ucd.status

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.role.UcdRole
import org.urbancode.ucadf.core.model.ucd.status.UcdStatus
import org.urbancode.ucadf.core.model.ucd.status.UcdStatusTypeEnum

class UcdGetStatuses extends UcAdfAction {
	// Action properties.
	/** The status type. */
	UcdStatusTypeEnum type
	
	/** (Optional) If specified then get statuses with names that match this regular expression. */
	String match = ""
	
	/**
	 * Runs the action.	
	 * @return Returns a list of status objects.
	 */
	@Override
	public List<UcdStatus> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdStatus> ucdStatuses

		logVerbose("Getting type [$type] statuses.")
				
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/status/getStatuses")
			.queryParam("type", type)
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdStatuses = response.readEntity(new GenericType<List<UcdStatus>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		List<UcdStatus> ucdReturnStatuses = []
		
		if (match) {
			for (ucdRole in ucdStatuses) {
				if (ucdRole.getName() ==~ match) {
					ucdReturnStatuses.add(ucdRole)
				}
			}
		} else {
			ucdReturnStatuses = ucdStatuses
		}
		
		return ucdReturnStatuses
	}
}
