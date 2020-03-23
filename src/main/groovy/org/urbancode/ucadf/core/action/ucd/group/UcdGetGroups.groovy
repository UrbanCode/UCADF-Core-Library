/**
 * This action gets a list of groups.
 */
package org.urbancode.ucadf.core.action.ucd.group

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.group.UcdGroup

class UcdGetGroups extends UcAdfAction {
	/**
	 * Runs the action.	
	 * @return The list of group objects.
	 */
	@Override
	public List<UcdGroup> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdGroup> ucdGroups = []
		
        WebTarget target = ucdSession.getUcdWebTarget().path("/security/group")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdGroups = response.readEntity(new GenericType<List<UcdGroup>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
				
		return ucdGroups
	}
}
