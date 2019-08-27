/**
 * This action cleans up cache versions that are no longer associated with a running request.
 */
package org.urbancode.ucadf.core.action.ucadf.cache

import org.urbancode.ucadf.core.action.ucd.applicationProcessRequest.UcdIsProcessRequestRunning
import org.urbancode.ucadf.core.action.ucd.version.UcdDeleteVersion
import org.urbancode.ucadf.core.action.ucd.version.UcdGetComponentVersions
import org.urbancode.ucadf.core.action.ucd.version.UcdGetVersionProperty
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcAdfCacheCleanProcessTemp extends UcAdfAction {
	// Action properties.
	/** (Optional) The directory to clean. */
	String dirName = ""
	
	/** The cache component name or ID. */
	String component = ""
	
	/** The current request ID that is running the action. */
	String currentRequestId = ""
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Clean the process temporary directory.
		if (dirName) {
			cleanProcessTempDir(dirName.trim())
		}
		
		// Clean out any unused cache component versions.
		if (component) {
			logInfo("Cleaning cache component versions from component [$component].")

			// Get the component versions.
			List<UcdVersion> ucdVersions = actionsRunner.runAction([
				action: UcdGetComponentVersions.getSimpleName(),
				component: component
			])
	
			for (UcdVersion ucdVersion in ucdVersions) {
				try {
					String requestId = actionsRunner.runAction([
						action: UcdGetVersionProperty.getSimpleName(),
						component: component,
						version: ucdVersion.getName(),
						property: UcAdfCachePutComponentVersion.UCDPROPNAME_PROCESSREQUESTID,
						returnAs: UcdGetVersionProperty.ReturnAsEnum.VALUE
					])
		
					// Determine if the process is the currently running process or another process request that is no longer running.
					Boolean deleteCache = false
					if (requestId.equals(currentRequestId)) {
						deleteCache = true
					} else {
						Boolean isProcessRequestRunning = actionsRunner.runAction([
							action: UcdIsProcessRequestRunning.getSimpleName(),
							requestId: requestId
						])
						
						if (!isProcessRequestRunning) {
							deleteCache = true
						}
					}
					
					if (deleteCache) {
						actionsRunner.runAction([
							action: UcdDeleteVersion.getSimpleName(),
							component: component,
							version: ucdVersion.getName()
						])
					}
				} catch(Exception e) {
					logInfo(e.getMessage())
				}
			}
		}		
	}	
	
	// Clean up a directory that has subdirectories associated with non-running process names.
	private cleanProcessTempDir(final String dirName) {
		logInfo("Cleaning temporary process directories from [$dirName].")
		
		File parentDir = new File(dirName.trim())
		
		for (dir in parentDir.listFiles()) {
			if (!dir.isDirectory()) {
				continue
			}
			
			if (dir.getName().split("-").length != 5) {
				continue
			}
			
			try {
				String requestId = dir.getName()

				// Determine if process request is running.
				Boolean isProcessRequestRunning = actionsRunner.runAction([
					action: UcdIsProcessRequestRunning.getSimpleName(),
					requestId: requestId
				])
				if (!isProcessRequestRunning) {
					logInfo("Deleting directory [${dir.getPath()}].")
					dir.deleteDir()
				}
			} catch(Exception e) {
				logInfo(e.getMessage())
			}
		}
	}
}
