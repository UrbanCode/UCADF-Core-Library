/**
 * This action gets a list of post-processing scripts.
 */
package org.urbancode.ucadf.core.action.ucd.postProcessing

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.postProcessing.UcdPostProcessingScript

class UcdGetPostProcessingScripts extends UcAdfAction {
	/**
	 * Runs the action.	
	 * @return Returns a list of post-processing script objects.
	 */
	@Override
	public List<UcdPostProcessingScript> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdPostProcessingScript> ucdPostProcessingScripts

		logInfo("Getting post processing scripts.")
				
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/script/postprocessing")
		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			ucdPostProcessingScripts = response.readEntity(new GenericType<List<UcdPostProcessingScript>>(){})
		} else {
            throw new UcdInvalidValueException(response)
		}
		
		return ucdPostProcessingScripts
	}
}
