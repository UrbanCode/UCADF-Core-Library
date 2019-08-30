/**
 * This action creates a status.
 */
package org.urbancode.ucadf.core.action.ucd.status

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdColorEnum
import org.urbancode.ucadf.core.model.ucd.status.UcdStatus
import org.urbancode.ucadf.core.model.ucd.status.UcdStatusTypeEnum

class UcdCreateStatus extends UcAdfAction {
	// Action properties.
	/** The status type. */
	UcdStatusTypeEnum type
	
	/** The name. */
	String name
	
	/** The color. */
	UcdColorEnum color

	/** (Optional) The description. */
	String description = ""
	
	/** The flag that indicates only one status may exist on an entity. */
	Boolean unique = false
	
	/** The flag that indicates fail if the status already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the status was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
				
		logInfo("Creating status type [$type] name [$name].")

		WebTarget target = ucdSession.getUcdWebTarget().path("/cli/status/createStatus")
			.queryParam("type", type)
			.queryParam("status", name)
			.queryParam("color", color.getValue())
			.queryParam("description", description)
			.queryParam("unique", unique)
		logDebug("target=$target")
		
		Response response = target.request().post(Entity.json())
		if (response.getStatus() == 200) {
			logInfo("Status type [$name] name [$name] created.")
			created = true
		} else {
			String errMsg = UcdInvalidValueException.getResponseErrorMessage(response)
			logInfo(errMsg)
			if (!(response.getStatus() == 400 && (errMsg ==~ /.*already exists.*/ && !failIfExists))) {
				throw new UcdInvalidValueException(errMsg)
			}
		}
		
		return created
	}
}
