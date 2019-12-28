/**
 * This action imports a component template.
 */
package org.urbancode.ucadf.core.action.ucd.componentTemplate

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.MultiPart
import org.urbancode.ucadf.core.action.ucd.genericProcess.UcdImportGenericProcess
import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtype
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.actionsrunner.UcAdfActionsRunner
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplateImport
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportActionEnum
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportTypeEnum
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j

@Slf4j
class UcdImportComponentTemplate extends UcAdfAction {
	// Action properties.
	/** The file name. */
	String fileName
	
	/** The component template upgrade type. */
	UcdImportTypeEnum compTempUpgradeType
	
	/** The generic process upgrade type. */
	UcdImportTypeEnum genProcessUpgradeType
	
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

		// Load the component object from a file.		
		UcdComponentTemplateImport ucdComponentTemplateImport = new ObjectMapper().readValue(
			importFile, 
			UcdComponentTemplateImport.class
		)

		// Import the component object.
		importComponentTemplate(
			actionsRunner,
			ucdComponentTemplateImport, 
			genProcessUpgradeType, 
			removeCrypt
		)
	}
	
	// Import a component template.
	// This is a static method so that it can be used by both the import component and import application actions.
	public static importComponentTemplate(
		final UcAdfActionsRunner actionsRunner,
		final UcdComponentTemplateImport ucdComponentTemplateImport, 
		final UcdImportTypeEnum genProcessUpgradeType, 
		final Boolean removeCrypt) {
		
		UcdSession ucdSession = actionsRunner.getUcdSession()
		
		String componentTemplate = ucdComponentTemplateImport.getName()

		// Determine whether to do an upgrade or an import action.
		UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
			action: UcdGetComponentTemplate.getSimpleName(),
			componentTemplate: componentTemplate,
			failIfNotFound: false
		])

		UcdImportActionEnum importAction
		if (ucdComponentTemplate) {
			importAction = UcdImportActionEnum.UPGRADE
		} else {
			importAction = UcdImportActionEnum.IMPORT
		}

		log.info("$importAction Component Template [$componentTemplate] into [${ucdSession.getUcdUrl()}] genProcessUpgradeType [$genProcessUpgradeType].")
		
		String jsonStr = ucdComponentTemplateImport.toJsonString(removeCrypt)

		// Replace plugin names as needed.
		jsonStr = UcdImportGenericProcess.replacePluginNames(
			ucdSession,
			jsonStr
		)
		
		log.info("\n" + new JsonBuilder(jsonStr).toPrettyString())
		
		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/componentTemplate/{importAction}")
			.resolveTemplate("importAction", importAction.getValue())
			.queryParam("processUpgradeType", genProcessUpgradeType)
		log.debug("target=$target".toString())

		MultiPart multiPart = new MultiPart()
		multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)
		multiPart.bodyPart(new FormDataBodyPart("file", jsonStr, MediaType.APPLICATION_OCTET_STREAM_TYPE))
		Response response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()))
		if (response.getStatus() != 200) {
			String errorMsg = response.readEntity(String.class)
			log.error("$errorMsg\n${errorMsg.replaceAll(/.*(Error importing.*?)&quot.*/, '$1')}")
			throw new UcdInvalidValueException("Status: ${response.getStatus()} Unable to import component template. $target")
		}
	}
}
