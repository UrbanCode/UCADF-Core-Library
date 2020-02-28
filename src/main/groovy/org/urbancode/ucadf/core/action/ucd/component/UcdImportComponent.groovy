/**
 * This action imports a component.
 */
package org.urbancode.ucadf.core.action.ucd.component

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
import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentImport
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportActionEnum
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportTypeEnum
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j

@Slf4j
class UcdImportComponent extends UcAdfAction {
	// Action properties.
	/** The import file name. */
	String fileName
	
	/** The component template upgrade type. */
	UcdImportTypeEnum compTempUpgradeType = UcdImportTypeEnum.UPGRADE_IF_EXISTS
	
	/** The generic process upgrade type. */
	UcdImportTypeEnum genProcessUpgradeType = UcdImportTypeEnum.UPGRADE_IF_EXISTS
	
	/** If true then removes any encrypted values from the import so that the keys for those encryptions aren't required. */
	Boolean removeCrypt = false
	
	/** Remove the team mappings from the imported objects. */
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
		UcdComponentImport ucdComponentImport = new ObjectMapper().readValue(
			importFile, 
			UcdComponentImport.class
		)

		// Import the component object.
		importComponent(
			actionsRunner,
			ucdComponentImport, 
			compTempUpgradeType, 
			genProcessUpgradeType, 
			removeCrypt,
			removeTeamMappings
		)
	}
	
	// Import a component.
	// This is a static method so that it can be used by both the import component and import application actions.
	public static importComponent(
		final UcAdfActionsRunner actionsRunner,
		final UcdComponentImport ucdComponentImport, 
		final UcdImportTypeEnum compTempUpgradeType, 
		final UcdImportTypeEnum genProcessUpgradeType, 
		final Boolean removeCrypt,
		final Boolean removeTeamMappings) {
		
		UcdSession ucdSession = actionsRunner.getUcdSession()
		
		String component = ucdComponentImport.getName()

		// If removing team mappings then clear the mappings in the loaded object.
		if (removeTeamMappings) {
			ucdComponentImport.setTeamMappings([])
		}

		// Determine if creating a new component or upgrading an existing one.
		UcdComponent ucdComponent = actionsRunner.runAction([
			action: UcdGetComponent.getSimpleName(),
			component: component,
			failIfNotFound: false
		])

		UcdImportActionEnum importAction
		if (ucdComponent) {
			importAction = UcdImportActionEnum.UPGRADE
		} else {
			importAction = UcdImportActionEnum.IMPORT
		}

		log.info("$importAction Component [$component] into [" + ucdSession.getUcdUrl() + "] compTempUpgradeType [$compTempUpgradeType] genProcessUpgradeType [$genProcessUpgradeType]")
		
		String jsonStr = ucdComponentImport.toJsonString(removeCrypt)

		// Replace plugin names as needed.
		jsonStr = UcdImportGenericProcess.replacePluginNames(
			ucdSession,
			jsonStr
		)
		
		log.info("\n" + new JsonBuilder(jsonStr).toPrettyString())

		WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/component/{importAction}")
			.resolveTemplate("importAction", importAction.getValue())
			.queryParam("upgradeType", compTempUpgradeType)
			.queryParam("processUpgradeType", genProcessUpgradeType)
		log.debug("target=$target".toString())
		
		MultiPart multiPart = new MultiPart()
		multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)
		multiPart.bodyPart(new FormDataBodyPart("file", jsonStr, MediaType.APPLICATION_OCTET_STREAM_TYPE))
		Response response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()))
		if (response.getStatus() != 200) {
			String errorMsg = response.readEntity(String.class)
			log.error("$errorMsg\n${errorMsg.replaceAll(/.*(Error importing.*?)&quot.*/, '$1')}")
			throw new UcdInvalidValueException("Status: ${response.getStatus()} Unable to import component. $target")
		}

        // Work around a 6.2 component template property inheritance problem.        
        if (ucdSession.isUcdVersion(UcdSession.UCDVERSION_62)) {
		    // Had to add this for 6.2.4.* to be able to work around a bug where a newly imported component
		    // wouldn't recognize it had properties from the associated template. Bug still exists in 6.2.5.
	        log.info("Fake update component [$component] to force template properties inheritance.")
	
	        Map requestMap = [
				name: component, 
				existingId: ucdComponent.getId()
			]

			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
	        log.info(jsonBuilder.toString())
			
	        target = ucdSession.getUcdWebTarget().path("/rest/deploy/component")
	        log.debug("target=$target")
	
	        response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
	        if (response.getStatus() != 200) {
				throw new UcdInvalidValueException(response)
	        }
	    }
	}
}
