/**
 * This action updates a component.
 * TODO: At this time this update is very limited in that it can only update a component name and a few other values.
 */
package org.urbancode.ucadf.core.action.ucd.component

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.componentTemplate.UcdGetComponentTemplate
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentTypeEnum
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.version.UcdVersionTypeEnum

import groovy.json.JsonBuilder

class UcdUpdateComponent extends UcAdfAction {
	/** The component name or ID. */
	String component

	/** (Optional) The new name. */	
	String name
	
	/** (Optional) The description. */
	String description

	/** (Optional) The component type. */	
	UcdComponentTypeEnum componentType
	
	/** (Optional) The template name or ID. */
	String template

	/** (Optional) The source configuration plugin name or ID. */
	String sourceConfigPlugin
			
	/** (Optional) The flag that indicates if automatic imports are to be done. */
	Boolean importAutomatically
	
	/** (Optional) The flag that indicates if VFS should be used. */
	Boolean useVfs
	
	/** (Optional) The version type. */
	UcdVersionTypeEnum defaultVersionType

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistInclude(
			[
				'component'
			]
		)
		
		// Get the component information.
		UcdComponent ucdComponent = actionsRunner.runAction([
			action: UcdGetComponent.getSimpleName(),
			component: component,
			failIfNotFound: true
		])

		// Get the component template ID.
		String templateId = template
		if (template && !UcdObject.isUUID(template)) {
			UcdComponentTemplate ucdAgent = actionsRunner.runAction([
				action: UcdGetComponentTemplate.getSimpleName(),
				actionInfo: false,
				template: template
			])
			
			templateId = ucdAgent.getId()
		}

		Map requestMap = [
			name: name ?: ucdComponent.getName(),
			description: description ?: ucdComponent.getDescription(),
			templateId: (templateId ?: (ucdComponent.getTemplate() ? ucdComponent.getTemplate().getId() : "")),
			componentType: componentType ?: ucdComponent.getComponentType(),
			sourceConfigPlugin: sourceConfigPlugin ?: (ucdComponent.getSourceConfigPlugin() ? ucdComponent.getSourceConfigPlugin().getId() : ""),
			importAutomatically: importAutomatically ?: ucdComponent.getImportAutomatically(),
			useVfs: useVfs ?: ucdComponent.getUseVfs(),
			defaultVersionType: defaultVersionType ?: ucdComponent.getDefaultVersionType(),
			existingId: ucdComponent.getId()
		]
		
		JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

        WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component")
		logDebug("target=$target")
		
		Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
		if (response.getStatus() == 200) {
			logInfo("Component [$component] updated.")
		} else {
			throw new UcdInvalidValueException(response)
		}
	}
}
