/**
 * This action installs a plugin.
 */
package org.urbancode.ucadf.core.action.ucd.plugin

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.MultiPart
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcdInstallAutomationPlugin extends UcAdfAction {
	/** The plugin file name. */
	String fileName
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		logVerbose("Installing plugin from file [$fileName] into [${ucdSession.getUcdUrl()}]")

		File fileObj = new File(fileName)
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/plugin/automationPlugin")
		logDebug("target=$target".toString())
		
		MultiPart multiPart = new MultiPart()
		multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)
		multiPart.bodyPart(new FormDataBodyPart("file", fileObj, MediaType.APPLICATION_OCTET_STREAM_TYPE))
		
		Response response
		try {
			response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()))
		} catch(Exception e) {
			throw new UcdInvalidValueException(e)
		}
		
		if (response.getStatus() == 200) {
			logVerbose("Plugin from file [$fileName] installed successfully.")
		} else {
            throw new UcdInvalidValueException(response)
		}
	}
}
