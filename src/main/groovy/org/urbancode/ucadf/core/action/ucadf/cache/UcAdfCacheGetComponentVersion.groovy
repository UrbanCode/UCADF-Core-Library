/**
 * This action gets the files from a cache component verison where they were previously put by a {@link UcAdfCachePutComponentVersion} action.
 */
package org.urbancode.ucadf.core.action.ucadf.cache

import org.urbancode.ucadf.core.action.ucd.version.UcdDownloadVersionFiles
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction

class UcAdfCacheGetComponentVersion extends UcAdfAction {
	// Action properties.
	/** The cache component name or ID. */
	String component
	
	/** The cache component version or ID. */
	String version
	
	/** The cache directory. */
	File cacheDir
	
	/** The flag that indicates fail if the cached component version is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		// A temporary directory for the download.
		File tempDir = new File("${cacheDir.getPath()}.temp")
		
		// The download artifacts file name.
		File artifactsFile = new File(tempDir, "${component}_${version}_artifacts.zip")

		// Download the cache component version artifacts file.
		Boolean downloaded = actionsRunner.runAction([
			action: UcdDownloadVersionFiles.getSimpleName(),
			component: component,
			version: version,
			fileName: artifactsFile.getPath(),
			extractDirName: cacheDir,
			failIfNotFound: failIfNotFound
		])

		if (downloaded) {
			// Delete the temporary directory.
			logInfo("Deleting temporary directory [${tempDir.getPath()}].")
			tempDir.deleteDir()
		}
	}
}
