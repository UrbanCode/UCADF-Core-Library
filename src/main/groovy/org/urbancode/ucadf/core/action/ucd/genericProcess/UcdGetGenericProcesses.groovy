/**
 * This action gets a list of generic processes.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess

class UcdGetGenericProcesses extends UcAdfAction {
	// Action properties.
	/** (Optional) If specified then get generic processes with names that match this regular expression. */
	String match = ""
	
	/**
	 * Runs the action.	
	 * @return The list of generic process objects.
	 */
	@Override
	public List<UcdGenericProcess> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdGenericProcess> ucdGenericProcesses = []
			
		logVerbose("Getting generic processes.")
	
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process")
		logDebug("target=$target")
	
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdGenericProcesses = response.readEntity(new GenericType<List<UcdGenericProcess>>(){})
		} else {
			throw new UcdInvalidValueException(response)
		}
		
		List<UcdGenericProcess> ucdReturnGenericProcesses = []
		
		if (match) {
			for (ucdGenericProcess in ucdGenericProcesses) {
				if (ucdGenericProcess.getName() ==~ match) {
					ucdReturnGenericProcesses.add(ucdGenericProcess)
				}
			}
		} else {
			ucdReturnGenericProcesses = ucdGenericProcesses
		}
		
		return ucdReturnGenericProcesses
	}	
}
