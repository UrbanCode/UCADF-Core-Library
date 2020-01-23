/**
 * This action creates a property definition.
 */
package org.urbancode.ucadf.core.action.ucd.property

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheetDef

import groovy.json.JsonBuilder

class UcdCreatePropDef extends UcAdfAction {
	// Action properties.
	/** The property sheet definition. */
	UcdPropSheetDef ucdPropSheetDef
	
	/** The property definition. */
	UcdPropDef ucdPropDef
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

        logVerbose("Add property definition [${ucdPropDef.getName()}].")

        Map requestMap = [
			name: ucdPropDef.getName(), 
            description: ucdPropDef.getDescription(),
            label: ucdPropDef.getLabel(),
            pattern: ucdPropDef.getPattern(),
            required: ucdPropDef.getRequired(),
            type: ucdPropDef.getType(),
            value: ucdPropDef.getValue(),
            definitionGroupId: ucdPropSheetDef.getId()
		]
		
        JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
        logDebug(jsonBuilder.toString())
        
        String resolveHttpValuesUrl = (ucdPropSheetDef.getResolveHttpValuesUrl() -~ /\/resolveHttpValues$/)
        WebTarget target = ucdSession.getUcdWebTarget().path("/{resolveHttpValuesUrl}/propDefs")
			.resolveTemplateFromEncoded("resolveHttpValuesUrl", resolveHttpValuesUrl)
        logDebug("target=$target")
        
        Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
        if (response.getStatus() == 200) {
            logVerbose("Added property defintion.")
        } else {
            logError(response.readEntity(String.class))
            throw new UcdInvalidValueException("Status: ${response.getStatus()} Unable to add property definition. $target")
        }
    }
}
