/**
 * This action finds resources in the resource tree.
 */
package org.urbancode.ucadf.core.action.ucd.resource

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterField
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldClassEnum
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldTypeEnum
import org.urbancode.ucadf.core.model.ucd.resource.UcdResource
import org.urbancode.ucadf.core.model.ucd.resource.UcdResourceTree

import groovy.json.JsonBuilder

class UcdFindResources extends UcAdfAction {
	/** The resource parent path or ID. Example: parent="/" and depth=1 will get top-level resources */
	String parent

	/** The filter fields. */	
	List<UcdFilterField> filterFields = []
	
	/** The number of levels beneath the parent. */
	Integer depth = 0
	
	/** The flag that indicates fail if the parent resource is not found. Default is true. */
	Boolean failIfNotFound = true

	private List<UcdResource> ucdResources = []
		
	/**
	 * Runs the action.	
	 * @return The list of resource objects.
	 */
	@Override
	public List<UcdResource> run() {
		// Validate the action properties.
		validatePropsExist()
		
		logInfo("Finding resources in [$parent] resource tree.")
		
		// Process top-level resource differently.		
		if ("".equals(parent) || "/".equals(parent)) {
			List<UcdResource> ucdTopResources = actionsRunner.runAction([
				action: UcdGetChildResources.getSimpleName(),
				resource: parent
			])
			
			for (ucdTopResource in ucdTopResources) {
				findResources(ucdTopResource.getPath(), 1)
			}
		} else {
			findResources(parent, 0)
		}

		logInfo("Found ${ucdResources.size()} resources.")

		return ucdResources
	}

	// Find the resources for the specified parent.	
	public findResources(
		final String parent,
		final Integer startDepth) {
		
		// Get the parent resource to get the resource ID to use for the find.			
		UcdResource ucdParentResource = actionsRunner.runAction([
			action: UcdGetResource.getSimpleName(),
			actionInfo: false,
			resource: parent,
			failIfNotFound: failIfNotFound
		])

		if (ucdParentResource) {		
			String resource = ucdParentResource.getId()

			// Construct the query fields.
			filterFields.add(
				new UcdFilterField(
					"active",
					"true",
					UcdFilterFieldTypeEnum.eq,
					UcdFilterFieldClassEnum.Boolean
				)
			)

			WebTarget target = UcdFilterField.addFilterFieldQueryParams(
				ucdSession.getUcdWebTarget().path("/rest/resource/resource/{resource}/resourcesTree").resolveTemplate("resource", resource),
				filterFields
			)
			
			logDebug("target=$target")
			
			// Get the resource tree.
			Map requestMap = [
				expandedNodeList: []
			]
			
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)

			UcdResourceTree ucdResourceTree
			Response response = target.request().post(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				ucdResourceTree = response.readEntity(UcdResourceTree.class)
			} else {
				throw new UcdInvalidValueException(response)
			}
			
			for (ucdChildResourceTree in ucdResourceTree.getRecords()) {
				processResourceTree(
					ucdChildResourceTree,
					startDepth
				)
			}
		}
	}
		
	// Recursive method to traverse child resources in a resource tree collection.	
	private processResourceTree(
		final UcdResourceTree ucdResourceTree,
		final Integer currentDepth) {

		// Add the resource tree entry.
		if (depth == 0 || depth == currentDepth) {
			ucdResources.add(ucdResourceTree)
		}
		
		// Recursively traverse the resource tree.
		for (childUcdResource in ucdResourceTree.getChildren()) {
			processResourceTree(
				childUcdResource,
				new Integer(currentDepth +  1)
			)
		}
	}
}
