/**
 * This action creates an environment.
 */
package org.urbancode.ucadf.core.action.ucd.environment

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.environment.UcdEnvironment
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdColorEnum

class UcdCreateEnvironment extends UcAdfAction {
	// Action properites.
	/** The application name or ID. */
	String application

	/** The name. */	
	String name
	
	/** The color. */
	UcdColorEnum color
	
	/** (Optional) The description. */
	String description = ""
	
	/** The flag that indicates require approvals. Default is false. */
	Boolean requireApprovals = false
	
	/** The flag that indicates fail if the environment already exists. Default is true. */
	Boolean failIfExists = true
	
	/**
	 * Runs the action.	
	 * @return True if the environment was created.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean created = false
		
		// Validate the environment doesn't already exist.
		if (failIfExists) {
			UcdEnvironment ucdEnvironment = actionsRunner.runAction([
				action: UcdGetEnvironment.getSimpleName(),
				application: application,
				environment: name,
				failIfNotFound: false
			])
			
			if (ucdEnvironment) {
				throw new UcAdfInvalidValueException("Application [$application] environment [$name] already exists.")
			}
		}
		
        logVerbose("Creating application [$application] environment [$name].")

        WebTarget target = ucdSession.getUcdWebTarget().path("/cli/environment/createEnvironment")
            .queryParam("application", application)
            .queryParam("name", name)
            .queryParam("description", description)
            .queryParam("color", color.getValue())
            .queryParam("requireApprovals", requireApprovals)
        logDebug("target=$target")
        
        Response response = target.request(MediaType.WILDCARD).put(Entity.json(""))
        // UCD 6.2 returns 200 and UCD 6.1 returns 201. Returns 200 if the environment already exists.
        if (response.getStatus() == 200 || response.getStatus() == 201) {
            logVerbose("Application [$application] environment [$name] created.")
			created = true
        } else {
            throw new UcAdfInvalidValueException(response)
        }
		
		return created
	}
}
