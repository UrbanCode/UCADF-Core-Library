/**
 * This action downloads a component version's file.
 */
package org.urbancode.ucadf.core.action.ucd.version

import java.nio.file.Files
import java.nio.file.Paths

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucadf.general.UcAdfExtractFile
import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponent
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentTypeEnum
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

class UcdDownloadVersionFiles extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The downloaded file name. If singleFilePath is specified then this will be the single file. Otherwise, the downloaded artifacts Zip file. */
	String fileName = ""
	
	/** (Optional) The extract directory name. If specified then the download will be extracted here and the Zip file removed. */
	String extractDirName = ""

	/** (Optional) A single file path. */	
	String singleFilePath = ""
	
	/** The flag that indicates to skip if the download file (Zip) already exists. Default is false. */
	Boolean skipIfZipExists = false
	
	/** The flag that indicates to skip if the extracted directory already exists. Default is false. */
	Boolean skipIfExtractDirExists = false

	/** The flag that indicates to delete the download file (Zip) after extracting. Default is true. */
	Boolean deleteZipIfExtracted = true

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

		if (skipIfZipExists && artifactsFile.exists()) {
			logVerbose("Skipping downloading artifacts file [${artifactsFile.getPath()}] that already exists for component [$component] version [$version].")
		} else if (extractDirName && skipIfExtractDirExists && new File(extractDirName).exists()) {
			logVerbose("Skipping downloading artifacts and extracting directory [${artifactsFile.getPath()}] that already exists for component [$component] version [$version].")
		} else {
			// Download the component version artifacts file.
			logVerbose("Downloading artifacts file [${artifactsFile.getPath()}] for component [$component] version [$version].")
	
			// Create the artifacts file target directory.
			artifactsFile.getParentFile()?.mkdirs()
	
	        // Make sure the component exists.
			UcdComponent ucdComponent = actionsRunner.runAction([
				action: UcdGetComponent.getSimpleName(),
				actionInfo: false,
				actionVerbose: false,
				component: component,
				failIfNotFound: failIfNotFound
			])
	
	        String useVersion = version
			UcdVersion ucdVersion
			
			if (ucdComponent) {
		        if ("latest".equals(version)) {
					UcdVersion ucdLatestVersion = actionsRunner.runAction([
						action: UcdGetComponentLatestVersion.getSimpleName(),
						actionInfo: false,
						actionVerbose: false,
						component: component,
						failIfNotFound: failIfNotFound
					])
		
					useVersion = ucdLatestVersion.getName()
		        }
		        
		        // Make sure the version exists.
				ucdVersion = actionsRunner.runAction([
					action: UcdGetVersion.getSimpleName(),
					actionInfo: false,
					actionVerbose: false,
					component: component,
					version: useVersion,
					failIfNotFound: failIfNotFound
				])
			}
			
			if (ucdVersion) {
				logVerbose("Starting download of component [$component] type [${ucdComponent.getComponentType()}] version [$version] type [${ucdVersion.getType()}].")

				WebTarget target
				if (singleFilePath) {
					target = ucdSession.getUcdWebTarget().path("/rest/deploy/version/{versionId}/downloadArtifact/{singleFilePath}")
						.resolveTemplate("versionId", ucdVersion.getId())
						.resolveTemplate("singleFilePath", singleFilePath)
				} else {
					target = ucdSession.getUcdWebTarget().path("/rest/deploy/version/{versionId}/downloadArtifacts")
						.resolveTemplate("versionId", ucdVersion.getId())
				}
				
				logDebug("target=$target")
				
				Response response = target.request().get()

				if (response.getStatus() == 404 && failIfNotFound && singleFilePath) {
					throw new UcAdfInvalidValueException("File [$singleFilePath] not found to download.")
				}
				
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
				
				logVerbose("Downloaded artifacts file [${artifactsFile.getAbsolutePath()}] size is [${artifactsFile.length()}] bytes.")
		
				downloaded = true

				// Extract the downloaded file.
				if (extractDirName) {
					File extractDir = new File(extractDirName)
					
					actionsRunner.runAction([
						action: UcAdfExtractFile.getSimpleName(),
						actionInfo: false,
						actionVerbose: actionVerbose,
						fileName: artifactsFile.getAbsolutePath(),
						extractDirName: extractDirName,
						skipIfExtractDirExists: skipIfExtractDirExists,
						deleteFileIfExtracted: deleteZipIfExtracted,
						actionVerbose: actionVerbose
					])
					
					// If this is a ZOS component then also unzip the package.zip files.
					if (UcdComponentTypeEnum.ZOS.equals(ucdComponent.getComponentType())) {
						// Extract the top-level package.zip file.
						actionsRunner.runAction([
							action: UcAdfExtractFile.getSimpleName(),
							actionInfo: false,
							actionVerbose: actionVerbose,
							fileName: new File(extractDir, "package.zip").getAbsolutePath(),
							extractDirName: extractDirName,
							skipIfExtractDirExists: skipIfExtractDirExists,
							deleteFileIfExtracted: deleteZipIfExtracted,
							actionVerbose: actionVerbose
						])

						// If it is a merged version then it will have child package ZIP files that need to be extracted.
						logVerbose("Looking for z/OS component package files in [$extractDirName].")
						for (packageZipPath in Files.walk(Paths.get(extractDirName))) {
							if (packageZipPath.getFileName().toString().matches(/^package_.*\.zip$/)) {
								logVerbose("Found [$packageZipPath].")

								// Extract the compressed package file that has a zip extension but is actually a tar file.
								actionsRunner.runAction([
									action: UcAdfExtractFile.getSimpleName(),
									actionInfo: false,
									actionVerbose: actionVerbose,
									fileName: packageZipPath.toString(),
									extractDirName: packageZipPath.toString().replaceAll(/.zip$/, ''),
									fileType: UcAdfExtractFile.ExtractFileTypeEnum.TAR,
									skipIfExtractDirExists: skipIfExtractDirExists,
									deleteFileIfExtracted: deleteZipIfExtracted,
									actionVerbose: actionVerbose
								])
							}
						}
					}
				}
			} else {
				logVerbose("Component [$component] version [$version] not found.")
			}
		}
		
		return downloaded
	}	
}
