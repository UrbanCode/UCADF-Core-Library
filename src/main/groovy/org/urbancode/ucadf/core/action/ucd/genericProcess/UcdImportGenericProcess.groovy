/**
 * This action imports a generic process.
 */
package org.urbancode.ucadf.core.action.ucd.genericProcess

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.MultiPart
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionsRunner
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcess
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcessImport
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportActionEnum
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j

@Slf4j
class UcdImportGenericProcess extends UcAdfAction {
	// Action properties.
	/** The file name. */
	String fileName
	
	/** This flag indicates if encryption strings should be removed. Default is false. */
	Boolean removeCrypt = false
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		File importFile = new File(fileName)

		// Load the generic process object from a file.		
		UcdGenericProcessImport ucdGenericProcessImport = new ObjectMapper().readValue(
			importFile, 
			UcdGenericProcessImport.class
		)

		// Import the generic process object.
		importGenericProcess(
			actionsRunner,
			ucdGenericProcessImport, 
			removeCrypt
		)
	}
	
	// Impport a generic process.
	// This is a static method so that it can be used by both the import component and import application actions.
	public static importGenericProcess(
		final UcAdfActionsRunner actionsRunner,
		final UcdGenericProcessImport ucdGenericProcessImport, 
		final Boolean removeCrypt = false) {
		
		UcdSession ucdSession = actionsRunner.getUcdSession()
		
		String process = ucdGenericProcessImport.getName()
		
		// Determine if creating a new generic process or upgrading an existing one.
		// Determine whether to do an upgrade or an import action.
		UcdGenericProcess ucdGenericProcess = actionsRunner.runAction([
			action: UcdGetGenericProcess.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
			process: process,
			failIfNotFound: false
		])

		UcdImportActionEnum importAction
		if (ucdGenericProcess) {
			importAction = UcdImportActionEnum.UPGRADE
		} else {
			importAction = UcdImportActionEnum.IMPORT
		}

		log.info("$importAction Generic Process [$process] into [${ucdSession.getUcdUrl()}]")
	
		String jsonStr = ucdGenericProcessImport.toJsonString(removeCrypt)

		// Replace plugin names as needed.
		jsonStr = UcdImportGenericProcess.replacePluginNames(
			ucdSession,
			jsonStr
		)

		log.info("\n" + new JsonBuilder(jsonStr).toPrettyString())

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/process/{importAction}")
			.resolveTemplate("importAction", importAction.getValue())
		log.debug("target=$target")

		MultiPart multiPart = new MultiPart()
		multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)
		multiPart.bodyPart(new FormDataBodyPart("file", jsonStr, MediaType.APPLICATION_OCTET_STREAM_TYPE))
		Response response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()))
		if (response.getStatus() != 200) {
			String errorMsg = response.readEntity(String.class)
			log.error("$errorMsg\n${errorMsg.replaceAll(/.*(Error importing.*?)&quot.*/, '$1')}")
			throw new UcAdfInvalidValueException("Status: ${response.getStatus()} Unable to import generic process. $target")
		}
	}
	
	// Replaces plugin names that have a different name in a newer UCD version.
	// This is used from multiple classes.
	public static String replacePluginNames(
		final UcdSession ucdSession,
		final String jsonStr) {

		String replacedJsonStr = jsonStr
		if (ucdSession.compareVersion(UcdSession.UCDVERSION_70) >= 0) {
			log.info("Replacing IBM UrbanCode plugin names with UrbanCode names.")
			replacedJsonStr = jsonStr.replaceAll('\"pluginName\":\"IBM UrbanCode', '\"pluginName\":\"UrbanCode')
		}
		return replacedJsonStr
	}
}
