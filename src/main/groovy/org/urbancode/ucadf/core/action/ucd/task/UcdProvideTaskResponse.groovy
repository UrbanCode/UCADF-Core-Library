/**
 * This action provides a response to a waiting task.
 */
package org.urbancode.ucadf.core.action.ucd.task

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.task.UcdTaskResponseEnum

import com.fasterxml.jackson.annotation.JsonProperty

import groovy.json.JsonBuilder

class UcdProvideTaskResponse extends UcAdfAction {
	// Action properties.
	/** The task ID. */
	String taskId
	
	/** The pass/fail value. */
	UcdTaskResponseEnum passFail
	
	/** (Optional) The task comment. */
	String comment = ""
	
	/** (Optional) The task properties. */
	@JsonProperty("properties")
	Properties taskProperties
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistExclude(
			[
				'taskProperties'
			]
		)

		logInfo("Providing task [$taskId] response [$passFail] comment [$comment].")

		// Initialize a request map.
		Map requestMap = [
			passFail: passFail.getValue(),
			comment: comment
		]

		// Add the custom properties to the request map.		
		if (taskProperties) {
			taskProperties.each { k, v ->
				requestMap.put(k, "p:$v")
			}
		}
		
        JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
        logInfo("jsonBuilder=$jsonBuilder")
		
        WebTarget target = ucdSession.getUcdWebTarget().path("/rest/approval/task/{taskId}/close")
			.resolveTemplate("taskId", taskId)
        logDebug("target=$target")
		
        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() != 200) {
            throw new UcdInvalidValueException(response)
        }
    }
}
