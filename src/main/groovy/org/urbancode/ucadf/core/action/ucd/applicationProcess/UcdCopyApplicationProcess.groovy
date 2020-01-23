/**
 * This action copies an application process from one application to another.
 */
package org.urbancode.ucadf.core.action.ucd.applicationProcess

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcessReplacement
import org.urbancode.ucadf.core.model.ucd.property.UcdPropDef
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

class UcdCopyApplicationProcess extends UcAdfAction {
	// Action properties.
	/** The from application name or ID. */
	String fromApplication
	
	/** The from application name or ID. */
	String fromProcess
	
	/** The to application name or ID. */
	String toApplication

	/** (Optional) The list of application process replacements expressions. If not specified then the default list is used. Which is fromname->toname and value: "***"->value: "" **/
	List<UcdApplicationProcessReplacement> replaceList
	
	/** If true then replace the existing process. Default is true. TODO: Is this needed? */
	Boolean replaceProcess = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'replaceList' ])
		
		// Initialize a default replace list if none provided.
		if (!replaceList) {
			replaceList = UcdApplicationProcessReplacement.getDefaultReplaceList(fromApplication, toApplication)
		}
		
		UcdApplicationProcess fromProcess = actionsRunner.runAction([
			action: UcdGetApplicationProcess.getSimpleName(),
			actionInfo: false,
			application: fromApplication,
			process: fromProcess
		])

		logVerbose("Copying application process [${fromProcess.getName()}] from application [$fromApplication] to [$toApplication] replace process [$replaceProcess].")
		
		logDebug("Original process.rootActivity\n" + new JsonBuilder(fromProcess.getRootActivity()).toPrettyString())
		logDebug("Original process.propDefs\n" + new JsonBuilder(fromProcess.getPropDefs()).toPrettyString())
		
		if (replaceList) {
			// Replace values in the rootActivity structure.
			String rootActivityStr = new JsonBuilder(fromProcess.getRootActivity()).toString()
			for (replace in replaceList) {
				rootActivityStr = rootActivityStr.replaceAll(replace.getFrom(), replace.getTo())
			}
			fromProcess.setRootActivity(new JsonSlurper().parseText(rootActivityStr))
			
			// Replace values in the propDefs structure
			String originalPropDefsStr = new JsonBuilder(fromProcess.getPropDefs()).toString()
			String propDefsStr = originalPropDefsStr
			for (replace in replaceList) {
				propDefsStr = propDefsStr.replaceAll(replace.getFrom(), replace.getTo())
			}
			
			// If the property definitions have changed as a result of the string replacement then set them back into the process object
			if (originalPropDefsStr != propDefsStr) {
				fromProcess.setPropDefs(new ObjectMapper().readValue(propDefsStr, new TypeReference<List<UcdPropDef>>(){}))
			}
		}

		// Fix HTTP property definition problems that vary with UCD versions.
        UcdPropDef.fixHttpPropDefs(
			toApplication, 
			fromProcess.getName(), 
			fromProcess.getPropDefs(),
			ucdSession
		)
		
		logDebug("Replaced process.rootActivity\n" + new JsonBuilder(fromProcess.getRootActivity()).toPrettyString())
		logDebug("Replaced process.propDefs\n" + new JsonBuilder(fromProcess.getPropDefs()).toPrettyString())

		if (replaceProcess) {
			// In the future, may need to preserve any application process properties before deleting and copy those to the new one.
			// In the future, may need to throw an exception and not delete if there's a secured property that can't be copied.
			actionsRunner.runAction([
				action: UcdDeleteApplicationProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: actionVerbose,
				application: toApplication,
				process: fromProcess.getName()
			])
		}

		// Create the application process.
		actionsRunner.runAction([
			action: UcdCreateApplicationProcess.getSimpleName(),
			actionInfo: false,
			actionVerbose: actionVerbose,
			application: toApplication,
			name: fromProcess.getName(),
			description: fromProcess.getDescription(),
			inventoryManagementType: fromProcess.getInventoryManagementType(),
			offlineAgentHandling: fromProcess.getOfflineAgentHandling(),
			propDefs: fromProcess.getPropDefs(),
			rootActivity: fromProcess.getRootActivity()
		])
	
		// Add the required role (works around an earlier UC bug where the role wasn't getting set.
		if (fromProcess.getRequiredRoleId()) {
			actionsRunner.runAction([
				action: UcdUpdateApplicationProcess.getSimpleName(),
				actionInfo: false,
				actionVerbose: actionVerbose,
				application: toApplication,
				process: fromProcess.getName(),
				requiredRole: fromProcess.getRequiredRoleId()
			])
		}
	}
}
