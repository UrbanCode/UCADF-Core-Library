/**
 * This action imports a application template.
 */
package org.urbancode.ucadf.core.action.ucd.applicationTemplate

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.MultiPart
import org.urbancode.ucadf.core.action.ucd.genericProcess.UcdImportGenericProcess
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplate
import org.urbancode.ucadf.core.model.ucd.applicationTemplate.UcdApplicationTemplateImport
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportActionEnum
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportTypeEnum

import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j

@Slf4j
class UcdImportApplicationTemplate extends UcAdfAction {
	// Action properties.
	/** The file name. */
	String fileName
	
	/** The generic process upgrade type. */
	UcdImportTypeEnum genProcessUpgradeType = UcdImportTypeEnum.UPGRADE_IF_EXISTS
	
	/** The resource template upgrade type. */
	UcdImportTypeEnum resourceTemplateUpgradeType = UcdImportTypeEnum.UPGRADE_IF_EXISTS
	
	/** The flag that indicates remove encrypted strings. */
	Boolean removeCrypt = false
	
	/** The flag that indicates remove team mappings. */
	Boolean removeTeamMappings = false
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		File importFile = new File(fileName)

		// Load the application object from a file.		
		UcdApplicationTemplateImport ucdApplicationTemplateImport = new ObjectMapper().readValue(
			importFile, 
			UcdApplicationTemplateImport.class
		)

		String applicationTemplate = ucdApplicationTemplateImport.getName()

		// Determine whether to do an upgrade or an import action.
		UcdApplicationTemplate ucdApplicationTemplate = actionsRunner.runAction([
			action: UcdGetApplicationTemplate.getSimpleName(),
			actionInfo: false,
			applicationTemplate: applicationTemplate,
			failIfNotFound: false
		])

		UcdImportActionEnum importAction
		Boolean isUpgrade
		if (ucdApplicationTemplate) {
			importAction = UcdImportActionEnum.UPGRADE
			isUpgrade = true
		} else {
			importAction = UcdImportActionEnum.IMPORT
			isUpgrade = false
		}

		logVerbose("$importAction Application Template [$applicationTemplate] into [${ucdSession.getUcdUrl()}] genProcessUpgradeType [$genProcessUpgradeType].")
		
		String jsonStr = ucdApplicationTemplateImport.toJsonString(removeCrypt)

		// Replace plugin names as needed.
		jsonStr = UcdImportGenericProcess.replacePluginNames(
			ucdSession,
			jsonStr
		)
		
		logVerbose("\n" + new JsonBuilder(jsonStr).toPrettyString())
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/applicationTemplate/import")
			.queryParam("processUpgradeType", genProcessUpgradeType)
			.queryParam("resourceTemplateUpgradeType", resourceTemplateUpgradeType)
			.queryParam("isUpgrade", isUpgrade)
		logVerbose("target=$target".toString())

		MultiPart multiPart = new MultiPart()
		multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)
		multiPart.bodyPart(new FormDataBodyPart("file", jsonStr, MediaType.APPLICATION_OCTET_STREAM_TYPE))
		Response response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()))
		if (response.getStatus() != 200) {
			String errorMsg = response.readEntity(String.class)
			logError("$errorMsg\n${errorMsg.replaceAll(/.*(Error importing.*?)&quot.*/, '$1')}")
			throw new UcAdfInvalidValueException("Status: ${response.getStatus()} Unable to import application template. $target")
		}
	}
}
