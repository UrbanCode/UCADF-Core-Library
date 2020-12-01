/**
 * This action extracts a compressed file.
 */
package org.urbancode.ucadf.core.action.ucadf.general

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException

class UcAdfExtractFile extends UcAdfAction {
	/** The extract file type. */
	enum ExtractFileType {
		ZIP
	}
	
	// Action properties.
	/** The extract file name. */
	String fileName

	ExtractFileType fileType = ExtractFileType.ZIP
		
	/** The extract directory name. */
	String extractDirName

	/** The flag that indicates to skip if the extracted directory already exists. Default is false. */
	Boolean skipIfExtractDirExists = false

	/** The flag that indicates to delete the extract file after extracting. Default is true. */
	Boolean deleteFileIfExtracted = true

	/**
	 * Runs the action.	
	 * @return True if the file was extracted.
	 */
	@Override
	public Boolean run() {
		// Validate the action properties.
		validatePropsExist()

		Boolean extracted = false
		
		File extractFile = new File(fileName)

		File extractDir = new File(extractDirName)
		
		if (skipIfExtractDirExists && new File(extractDirName).exists()) {
			logVerbose("Skipping extracting [${extractFile.getPath()}] because sipIfExtractDirExists was specified and [$extractDirName] already exists.")
		} else {
			logVerbose("Extracting file [${extractFile.getPath()}] to directory [${extractDir.getPath()}].")
			
			// Create the empty target directory.
			extractDir.mkdirs()

			// Currently only supports Zip file type.
			if (ExtractFileType.ZIP.equals(fileType)) {
				// Unzip the file.
				unzip(
					extractFile, 
					extractDir,
					actionVerbose
				)	
			}
			
			// Delete the extract file.
			if (deleteFileIfExtracted) {
				try {
					new File(extractFile.getPath()).delete()
				} catch (Exception e) {
					logVerbose("Ignoring error deleting file ${extractFile.getPath()}. ${e.getMessage()}")
				}
			}
		}
		
		return extracted
	}

	// Unzip the extract file.	
	public static unzip(
		final File extractFile,
		final File extractDir,
		final Boolean verbose) {

		if (verbose) {
			println "Unzipping file [$extractFile] to directory [$extractDir]."
		}
		
		// Make sure the extract directory exists.
		extractDir.mkdirs()

		try {
			byte[] buffer = new byte[1024]
			FileInputStream inputStream = new FileInputStream(extractFile)
			ZipInputStream zipInputStream = new ZipInputStream(inputStream)
			
			ZipEntry zipEntry = zipInputStream.getNextEntry()
			if (!zipEntry) {
				throw new UcAdfInvalidValueException("Unable to get information from zip file [$extractFile].")
			}
			
			while(zipEntry != null) {

				File newFile = new File("${extractDir.getPath()}${File.separator}${zipEntry.getName()}")
				if (verbose) {
					println "Unzipping ${newFile.getPath()}"
				}
				
				if (zipEntry.isDirectory()){
					// Create a directory.
					newFile.mkdirs()
				} else {
					// Extract a file.
					new File(newFile.getParent()).mkdirs()
					FileOutputStream outputStream = new FileOutputStream(newFile)
					int len
					while ((len = zipInputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, len)
					}
					
					outputStream.close()
					zipInputStream.closeEntry()
					
				}
				
				// Set the time stamp.
				newFile.setLastModified(zipEntry.getTime())
				
				zipEntry = zipInputStream.getNextEntry()
			}
			//close last ZipEntry
			zipInputStream.closeEntry()
			zipInputStream.close()
			inputStream.close()
		} catch (IOException e) {
			e.printStackTrace()
		}
	}
}
