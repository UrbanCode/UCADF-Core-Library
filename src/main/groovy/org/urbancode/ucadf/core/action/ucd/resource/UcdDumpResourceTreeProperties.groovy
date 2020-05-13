/**
 * This action dumps the resource tree properties.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource

class UcdDumpResourceTreeProperties extends UcAdfAction {
	/** The resource path or ID. */
	String resource
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Get the resource information.
		UcdResource ucdResource = actionsRunner.runAction([
			action: UcdGetResource.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			resource: resource,
			failIfNotFound: true
		])

		// Recursively processes the resources.		
		dumpResourceTreeProperties(ucdResource)
	}
		
	public dumpResourceTreeProperties(
		final UcdResource ucdResource, 
		final Integer indent = 0) {

		List<UcdProperty> ucdProperties = actionsRunner.runAction([
			action: UcdGetResourceProperties.getSimpleName(),
			actionInfo: false,
			actionVerbose: actionVerbose,
			resource: ucdResource.getId(),
			failIfNotFound: false
		])
		
		if (ucdProperties.size() > 0) {
			println "\t".multiply(indent) + ucdResource.getPath()
			for (property in ucdProperties) {
				println "\t".multiply(indent + 1) + "${property.getName()} [${property.getValue()}]" + (property.getSecure() ? " (secure)" : "")
			}
		}
		
		List<UcdResource> ucdChildResources = actionsRunner.runAction([
			action: UcdGetChildResources.getSimpleName(),
			actionInfo: false,
			actionVerbose: actionVerbose,
			resource: ucdResource.getId()
		])
		
		for (ucdChildResource in ucdChildResources) {
			dumpResourceTreeProperties(ucdChildResource, indent + 1)
		}
	}
}
