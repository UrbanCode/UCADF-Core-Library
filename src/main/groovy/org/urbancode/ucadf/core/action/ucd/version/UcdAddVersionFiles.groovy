/**
 * This action adds files (artifacts) to a component version.
 */
package org.urbancode.ucadf.core.action.ucd.version

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.security.MessageDigest

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.FormDataContentDisposition
import org.glassfish.jersey.media.multipart.FormDataMultiPart
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.version.UcdVersion

import com.fasterxml.jackson.annotation.JsonIgnore

import groovy.json.JsonBuilder
import io.github.azagniotov.matcher.AntPathMatcher

class UcdAddVersionFiles extends UcAdfAction {
	// Action properties.
	/** The component name or ID. */
	String component
	
	/** The version name or ID. */
	String version
	
	/** The base directory from which all files will be processed according to the include/exclude rules. */
	String base
	
	/** (Optional) Target path offset (the directory in the version files to which these files should be added). */
	String offset = ""
	
	/** (Optional) A list of include file Ant FileSet patterns for selecting files to add. */
	List<String> include = []
	
	/** (Optional) A list of exclude file Ant FileSet patterns for exluding files (overrides includes). */
	List<String> exclude = []

	/** Save execute bits for files. Default is false. */	
	Boolean saveExecuteBits = false

	/** The flag that indicates fails if no files are added. Default is false. */
	Boolean failIfNoFiles = false

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()

		// Ant FileSet pattern matcher.
        AntPathMatcher antPathMatcher = new AntPathMatcher.Builder().build()

        logVerbose("Adding component [$component] version [$version] base [$base] saveExecuteBits [$saveExecuteBits] "
			+ (include.size() > 0 ? "include $include " : "")
			+ (exclude.size() > 0 ? "exclude $exclude " : "")
		)

		// Work around 7.0 bug where it converts a version name with 4 hyphens to a UUID.
		if (isIncorrectlyInterpretedAsUUID(version)) {
			UcdVersion ucdVersion = actionsRunner.runAction([
				action: UcdGetVersion.getSimpleName(),
				actionInfo: false,
				component: component,
				version: version,
				failIfNotFound: true
			])
			
			version = ucdVersion.getId()
		}
		
		// The base directory to be searched.
		File baseDir = new File(base)

		if (!baseDir.exists()) {
			throw new UcAdfInvalidValueException("Base directory [$baseDir] not found.")
		}
		
		// Derive a base directory path with forward slashes.
		String baseDirAbsolutePath = getCanonicalPath(baseDir)

		logVerbose("Finding files in base directory [${baseDir.getCanonicalPath()}].")

		Integer fileCount = 0
				
		// Process the directory entries recursively.		
		baseDir.eachFileRecurse { file ->
			String fileAbsolutePath = getCanonicalPath(file)
			String parentAbsolutePath = getCanonicalPath(file.getParentFile())
			String fileName = fileAbsolutePath.replaceAll(/^$parentAbsolutePath[\/]/, "")
			String fileRelativePath = fileAbsolutePath.replaceAll(/^$baseDirAbsolutePath[\/]/, "")
			
			Boolean addFile = true

			// If includes were specified then determine if the file should be added to the list.
			if (include.size() > 0) {
				include.find { includeMatch ->
					addFile = antPathMatcher.isMatch(includeMatch, fileRelativePath)		
				}
			}
			
			// If excludes were specified then determine if the file should be excluded from the list.
			if (addFile && exclude.size() > 0) {
				exclude.find { excludeMatch ->
					addFile = !(antPathMatcher.isMatch(excludeMatch, file.getName()))
				}
			}

			// Add the file to the version.			
			if (addFile) {
				addFileVersion(
					fileAbsolutePath,
					fileRelativePath
				)
				
				fileCount++
			}
		}
	
		logVerbose("Added [$fileCount] files to component [$component] version [$version].")
		
		if (fileCount == 0 && failIfNoFiles) {
			throw new UcAdfInvalidValueException("No files were found to add.")
		}
	}

	// Add the file to the component version.
	private addFileVersion(
		final String filePath,
		final String fileRelativePath) {

		// Prepend the optional offset.		
		String fileUploadPath = fileRelativePath
		if (offset) {
			fileUploadPath = [ offset, fileRelativePath].join("/")
		}
	
		File file = new File(filePath)
		
		if (actionVerbose) {
			print "Adding " + (file.isFile() ? "file" : "directory") + " [$filePath]"
		}
		
		// Create the multipart form for the API call.
		Map entryMetadataMap

		// Initialize the metadata map.
		String checkSum
		if (file.isFile()) {
			// Set up metadata for a file.
			checkSum = getChecksum(
				MessageDigest.getInstance("SHA-256"),
				file
			)

			entryMetadataMap = [
				"path": fileUploadPath,
				"version": 1,
				"type": "REGULAR",
				"length": file.length(),
				"modified": file.lastModified(),
				"contentHash": "SHA-256{" + checkSum + "}"
			]

			// If save execute bits option then add that information to the metadata map.
			if (saveExecuteBits) {
				// Determine if Windows or Linux.
				String osName = System.properties['os.name']
				if (!(osName.toLowerCase().contains('windows'))) {
					Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(Paths.get(file.getCanonicalPath()))
					String mode = toModeString(permissions)

					entryMetadataMap.put("unixPermissions", "$mode::")
				}
			}
		} else {
			// Set up metadata for a directory.
			entryMetadataMap = [
				"path": fileUploadPath,
				"version": 1,
				"type": "DIRECTORY",
				"length": 0,
				"modified": file.lastModified()
			]
		}

		if (actionVerbose) {
			println " as $entryMetadataMap."
		}
		
		FormDataMultiPart formDataMultiPart = new FormDataMultiPart()
			.field("component", component, MediaType.TEXT_PLAIN_TYPE)
			.field("version", version, MediaType.TEXT_PLAIN_TYPE)
			.field("entryMetadata", new JsonBuilder(entryMetadataMap).toString(), MediaType.TEXT_PLAIN_TYPE)

		// If the entry is a file then attach it.
		if (file.isFile()) {
			// Add the file to the multipart.
			formDataMultiPart.bodyPart(
				new FormDataBodyPart(
					FormDataContentDisposition.name("entryContent-$checkSum").fileName(filePath).build(),
					file,
					MediaType.APPLICATION_OCTET_STREAM_TYPE
				)
			)
		}
					
		WebTarget target = ucdSession.getUcdWebTarget().path("/cli-internal/version/addVersionFilesFull")
		logDebug("target=$target")

		Response response = target.request().post(Entity.entity(formDataMultiPart, formDataMultiPart.getMediaType()))
		if (response.getStatus() != 204) {
			throw new UcAdfInvalidValueException(response)
		}
	}

	// Get a file checksum.
	private static String getChecksum(
		final MessageDigest messageDigest, 
		final File file) throws IOException {
		
	    FileInputStream fileInputStream = new FileInputStream(file)
			
	    byte[] fileBytes = new byte[1024]
	    int totalBytes = 0 
	
		try {	
		    while((totalBytes = fileInputStream.read(fileBytes)) != -1) {
		        messageDigest.update(
					fileBytes, 
					0, 
					totalBytes
				)
		    }
		} finally {
			try {
				fileInputStream.close()
			} catch(Exception e) {
				// Ignore exceptions.
			}
		}
	
	    StringBuilder stringBuilder = new StringBuilder()
	
	    byte[] messageDigestBytes = messageDigest.digest()
	    for(int i = 0; i < messageDigestBytes.length ;i++) {
	        stringBuilder.append(
				Integer.toString((messageDigestBytes[i] & 0xff) + 0x100, 16).substring(1)
			)
	    }
	
	   return stringBuilder.toString()
	}

	@JsonIgnore
	private String getCanonicalPath(final File file) {
		return file.getCanonicalPath().replaceAll("\\\\", "/")
	}
	
	// Convert a Posix file permission to a mode octal string.
	private String toModeString(final Set<PosixFilePermission> posixFilePermissions) {
	    int mode = 0
		
	    for (PosixFilePermission posixFilePermission : posixFilePermissions) {
	        switch(posixFilePermission) {
	            case PosixFilePermission.OWNER_READ:
	                mode |= 0400
	                break
	            case PosixFilePermission.OWNER_WRITE:
	                mode |= 0200
	                break
	            case PosixFilePermission.OWNER_EXECUTE:
	                mode |= 0100
	                break
	            case PosixFilePermission.GROUP_READ:
	                mode |= 0040
	                break
	            case PosixFilePermission.GROUP_WRITE:
	                mode |= 0020
	                break
	            case PosixFilePermission.GROUP_EXECUTE:
	                mode |= 0010
	                break
	            case PosixFilePermission.OTHERS_READ:
	                mode |= 0004
	                break
	            case PosixFilePermission.OTHERS_WRITE:
	                mode |= 0002
	                break
	            case PosixFilePermission.OTHERS_EXECUTE:
	                mode |= 0001
	                break
	            default:
					throw new UcAdfInvalidValueException("Invalid permission value [$posixFilePermission].")
	        }
	    }

	    return sprintf("%04o", mode)
	}
}
