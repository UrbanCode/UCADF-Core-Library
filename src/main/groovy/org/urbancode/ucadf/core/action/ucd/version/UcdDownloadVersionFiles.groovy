/**
 * This action downloads a component version's file.
 */
package org.urbancode.ucadf.core.action.ucd.version

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdDownloadVersionFiles extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The downlaoded ZIP file name. */
	String fileName
	
	/** (Optional) The extract directory name. If specified then the download will be extracted here and the ZIP file removed. */
	String extractDirName = ""

	/** The single file path. */	
	String singleFilePath = ""
	
	/** The flag that indicates fail if the version is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean downloaded = false
		File artifactsFile = new File(fileName)

		// Download the component version artifacts file.
		logInfo("Downloading artifacts file [${artifactsFile.getPath()}] for component [$component] version [$version].")

		// Create the artifacts file target directory.
		artifactsFile.getParentFile()?.mkdirs()

        // Make sure the component exists.
		UcdComponent ucdComponent = actionsRunner.runAction([
			action: UcdGetComponent.getSimpleName(),
			actionInfo: false,
			component: component,
			failIfNotFound: true
		])

        String useVersion = version
        if ("latest".equals(version)) {
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetComponentLatestVersion.getSimpleName(),
				actionInfo: false,
				component: component,
				failIfNotFound: true
			])

			useVersion = ucdVersion.getName()
        }
        
        // Make sure the version exists.
		UcdVersion ucdVersion = actionsRunner.runAction([
			action: UcdGetVersion.getSimpleName(),
			actionInfo: false,
			component: component,
			version: version,
			failIfNotFound: true
		])

		logInfo("Starting download of component [$component] version [$version].")
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/version/{versionId}/downloadArtifacts")
			.resolveTemplate("versionId", ucdVersion.getId())
		logDebug("target=$target")
		
		Response response = target.request().get()
		
		InputStream inputStream = response.readEntity(InputStream.class)

		FileOutputStream fileOutputStream = new FileOutputStream(artifactsFile)

		try {
			byte[] dataBuffer = new byte[1024]
			int bytesRead
			while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead)
			}
		} finally {
			try {
				inputStream.close()
			} catch (Exception e) {
				// Ignore close exception.
			}
			
			try {
				fileOutputStream.close()
			} catch (Exception e) {
				// Ignore close exception.
			}
		}
		
		logInfo("Downloaded artifacts file [${artifactsFile.getAbsolutePath()}] size is [${artifactsFile.length()}] bytes.")

		downloaded = true
		
		// Extract the downloaded file.
		if (extractDirName) {
			File extractDir = new File(extractDirName)
			
			logInfo("Extracting download file [${artifactsFile.getPath()}] to directory [${extractDir.getPath()}].")
			
			// Create the empty target directory.
			extractDir.mkdirs()
	
			AntBuilder antBuilder = new AntBuilder()
			
			// Determine if Windows or Linux.
			String osName = System.properties['os.name']
			if (osName.toLowerCase().contains('windows')) {
				// Use Ant library to extract the artifacts file. This does not preserve permissions correctly on Linux.
				antBuilder.unzip(
					src:artifactsFile.getPath(), 
					dest:extractDir.getPath(), 
					overwrite:"false"
				)
			} else {
				// Use the unzip command on Linux so that permissions are preserved on the extracted files.
				List commandList = [ 
					"unzip",
					artifactsFile.getPath(),
					"-d",
					extractDir.getPath()
				]
				println commandList
				
				UcdSession.executeCommand(
					commandList, 
					600, 
					true, 
					true
				)
			}
			
			// Use Ant library to delete the zip file.
			// Using this step assures the file gets deleted in situations where a normal File delete hasn't been working.
			antBuilder.delete(
				file: artifactsFile.getPath(), 
				failonerror: false
			)  
		}
		
		return downloaded
	}	
}
