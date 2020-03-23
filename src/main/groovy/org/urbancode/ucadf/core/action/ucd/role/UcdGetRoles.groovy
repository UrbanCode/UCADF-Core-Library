/**
 * This action gets a list of roles.
 */
package org.urbancode.ucadf.core.action.ucd.role

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.role.UcdRole

class UcdGetRoles extends UcAdfAction {
	// Action properties.
	/** (Optional) If specified then get roles with names that match this regular expression. */
	String match = ""

	/**
	 * Runs the action.
	 * @return Returns a list of role objects.
	 */
	@Override
	public List<UcdRole> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdRole> ucdRoles = []
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/security/role")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdRoles = response.readEntity(new GenericType<List<UcdRole>>(){})
		} else {
            throw new UcAdfInvalidValueException(response)
		}
		
		List<UcdRole> ucdReturnRoles = []
		
		if (match) {
			for (ucdRole in ucdRoles) {
				if (ucdRole.getName() ==~ match) {
					ucdReturnRoles.add(ucdRole)
				}
			}
		} else {
			ucdReturnRoles = ucdRoles
		}
		
		return ucdReturnRoles
	}
}
