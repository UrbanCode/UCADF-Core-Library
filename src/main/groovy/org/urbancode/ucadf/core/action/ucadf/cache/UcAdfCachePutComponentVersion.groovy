/**
 * This action caches the contents of a directory to a component verison when it can later be used by the {@link UcAdfCacheGetComponentVersion} action to get the files.
 */
package org.urbancode.ucadf.core.action.ucadf.cache

import org.urbancode.ucadf.core.action.ucd.version.UcdAddVersionFiles
import org.urbancode.ucadf.core.action.ucd.version.UcdCreateVersion
import org.urbancode.ucadf.core.action.ucd.version.UcdSetVersionProperties
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

class UcAdfCachePutComponentVersion extends UcAdfAction {
	// Constants.
	public final static UCDPROPNAME_PROCESSREQUESTID = "ucAdfProcessRequestId"

	// Action properties.
	/** The component name or ID. */	
	String component
	
	/** The version name or ID. */
	String version
	
	/** The name of the directoriy with the contents to be cached. */
	String cacheDirName

	/** The process request ID to be associated with the cached version. */
	String processRequestId
	
	/** The maximum cache size. */
	Long cacheMaxSize = 100000
	
	/** The flag that indicates fail if the cache directory is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		File cacheDir = new File(cacheDirName)
		
		// If the cache directory doesn't exist then don't process it.
		if (cacheDir.exists()) {
			// Validate the size of the directory.
			Long dirSize = cacheDir.directorySize()
			logVerbose("Size of directory is [$dirSize] bytes and maximum allowed size is [$cacheMaxSize].")
			if (dirSize > cacheMaxSize) {
				throw new UcdInvalidValueException("Directory size is > maximum allowed size [$cacheMaxSize].")
			}
			
			// Create the version if it doesn't already exist.
			actionsRunner.runAction([
				action: UcdCreateVersion.getSimpleName(),
				actionInfo: false,
				component: component,
				name: version,
				failIfExists: false
			])
	
			// Set the process request ID property on the version.
			actionsRunner.runAction([
				action: UcdSetVersionProperties.getSimpleName(),
				actionInfo: false,
				component: component,
				version: version,
				properties: [
					[ 
						name: UCDPROPNAME_PROCESSREQUESTID, 
						value: processRequestId
					]
				]
			])

			// Add the files to the version.
			actionsRunner.runAction([
				action: UcdAddVersionFiles.getSimpleName(),
				actionInfo: false,
				actionVerbose: actionVerbose,
				component: component,
				version: version,
				base: cacheDir.getPath(),
				include: [ "**/*" ]
			])
		} else {
			if (failIfNotFound) {
				throw new UcdInvalidValueException("Cache directory [${cacheDir.getPath()}] not found.")
			}
		}
	}	
}
