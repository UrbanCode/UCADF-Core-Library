/**
 * This action adds an agent relay to teams.
 *<p>
 * Import application with more extensive capabilities than those provided by the import API.
 * Allows components, templates, processes, environments to be filtered out rather than importing all of them
 * so that applications can be imported without impacting shared entities they may be consuming.
 */
package org.urbancode.ucadf.core.action.ucd.application

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.MultiPart
import org.urbancode.ucadf.core.action.ucd.component.UcdImportComponent
import org.urbancode.ucadf.core.action.ucd.componentTemplate.UcdGetComponentTemplate
import org.urbancode.ucadf.core.action.ucd.componentTemplate.UcdImportComponentTemplate
import org.urbancode.ucadf.core.action.ucd.genericProcess.UcdImportGenericProcess
import org.urbancode.ucadf.core.action.ucd.security.UcdGetSecuritySubtype
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.application.UcdApplication
import org.urbancode.ucadf.core.model.ucd.application.UcdApplicationImport
import org.urbancode.ucadf.core.model.ucd.component.UcdComponentImport
import org.urbancode.ucadf.core.model.ucd.componentTemplate.UcdComponentTemplate
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcessImport
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportActionEnum
import org.urbancode.ucadf.core.model.ucd.importExport.UcdImportTypeEnum
import org.urbancode.ucadf.core.model.ucd.security.UcdSecuritySubtype
import org.urbancode.ucadf.core.model.ucd.system.UcdSession
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamSecurity

import com.fasterxml.jackson.databind.ObjectMapper

class UcdImportApplication extends UcAdfAction {
	/** The import file name. */
	String fileName
	
	/** The application upgrade type. Default is UPGRADE_IF_EXISTS. */
	UcdImportTypeEnum appUpgradeType = UcdImportTypeEnum.UPGRADE_IF_EXISTS
	
	/** The component upgrade type. Default is UPGRADE_IF_EXISTS. */
	UcdImportTypeEnum compUpgradeType = UcdImportTypeEnum.UPGRADE_IF_EXISTS
	
	/** The component template upgrade type. Default is UPGRADE_IF_EXISTS. */
	UcdImportTypeEnum compTempUpgradeType = UcdImportTypeEnum.UPGRADE_IF_EXISTS
	
	/** The generic process upgrade type. Default is UPGRADE_IF_EXISTS. */
	UcdImportTypeEnum genProcessUpgradeType = UcdImportTypeEnum.UPGRADE_IF_EXISTS
	
	/** If true then removes any encrypted values from the import so that the keys for those encryptions aren't required. */
	Boolean removeCrypt = false
	
	/** Remove the team mappings from the imported objects. */
	Boolean removeTeamMappings = false
	
	/** Import the components with names matching the regular expression. */
	String compMatch
	
	/** Import the component templates with names matching the regular expression. */
	String compTempMatch
	
	/** Import the generic processes with names matching the regular expression. */
	String genProcessMatch
	
	/** Import the application environments with names matching the regular expression. */
	String envMatch
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action propertis.
		validatePropsExist()

		// Load the application object from a file.
		File importFile = new File(fileName)
		UcdApplicationImport ucdApplicationImport = new ObjectMapper().readValue(
			importFile, 
			UcdApplicationImport.class
		)
		
		String application = ucdApplicationImport.getName()
		logInfo("Processing import for [$application] appUpgradeType [$appUpgradeType] compUpgradeType [$compUpgradeType] compTempUpgradeType [$compTempUpgradeType] genProcessUpgradeType [$genProcessUpgradeType] removeCrypt [$removeCrypt] compMatch [$compMatch] compTempMatch [$compTempMatch] genProcessMatch [$genProcessMatch]")

		// Validate the application upgrade options.
		UcdApplication ucdApplication = actionsRunner.runAction([
			action: UcdGetApplication.getSimpleName(),
			application: application,
			failIfNotFound: false
		])

		if (ucdApplication && appUpgradeType == UcdImportTypeEnum.FAIL_IF_EXISTS) {
			throw new UcdInvalidValueException("Application already exists.")
		}
		
		if (!ucdApplication && appUpgradeType == UcdImportTypeEnum.FAIL_IF_DOESNT_EXIST) {
			throw new UcdInvalidValueException("Application doesn't exist.")
		}

		// Import the generic processes.
		for (processImport in ucdApplicationImport.getAllGenericProcessImports(compMatch, compTempMatch, genProcessMatch).values()) {
			UcdImportGenericProcess.importGenericProcess(
				actionsRunner,
				processImport, 
				removeCrypt
			)
		}

		// Import the application component templates.
		for (ucdComponentTemplateImport in ucdApplicationImport.getComponentTemplateImports(compMatch, compTempMatch).values()) {
			// Will always use UcdImportRule.FAIL_IF_DOESNT_EXIST for the generic process import option here so that this import only affects the templates, not the generic processes the template may use
			UcdImportComponentTemplate.importComponentTemplate(
				actionsRunner,
				ucdComponentTemplateImport,
				UcdImportTypeEnum.FAIL_IF_DOESNT_EXIST, 
				removeCrypt
			)
		}

		// Import the application components.
		for (ucdComponentImport in ucdApplicationImport.getComponentImports(compMatch).values()) {
			// In UCD 6.1 we always used UcdImportRule.FAIL_IF_DOESNT_EXIST for options here so that this import only affects the components, 
            // not the templates or generic processes.
            // In UCD 6.2 this option is broken so we use the UcdImportRule.USE_EXISTING_IF_EXISTS option but validate the template exists first.
            if (ucdComponentImport.getComponentTemplate() && ucdComponentImport.getComponentTemplate().getName()) {
                String componentTemplate = ucdComponentImport.getComponentTemplate().getName()
                logInfo("Component [${ucdComponentImport.getName()}] uses template [$componentTemplate}.")

				UcdComponentTemplate ucdComponentTemplate = actionsRunner.runAction([
					action: UcdGetComponentTemplate.getSimpleName(),
					componentTemplate: componentTemplate,
					failIfNotFound: true
				])
            }
			
            // Import the component.
			UcdImportComponent.importComponent(
				actionsRunner,
                ucdComponentImport, 
                UcdImportTypeEnum.USE_EXISTING_IF_EXISTS, 
                UcdImportTypeEnum.FAIL_IF_DOESNT_EXIST, 
                removeCrypt,
				removeTeamMappings
            )
		}

		// Determine if the application import needs to be done. Only appUpgradeType when it won't be done is USE_EXISTING_IF_EXISTS.
		List<UcdTeamSecurity> reAddTeamSubtypes = []
		if (appUpgradeType != UcdImportTypeEnum.USE_EXISTING_IF_EXISTS) {
			// Save the list of components and set the application import components list to empty so that no components are imported with the application.
			List<UcdComponentImport> savedComponents = ucdApplicationImport.getComponents()
			ucdApplicationImport.setComponents(new ArrayList<UcdComponentImport>())
			
			// Set the generic processes list to empty so that no components are imported with the application.
			ucdApplicationImport.setGenericProcesses(new ArrayList<UcdGenericProcessImport>())
	
			// Only keep the desired environments.
			ucdApplicationImport.filterEnvironments(envMatch)
	
			// Determine if creating a new application or upgrading an existing one.
			UcdImportActionEnum importAction
			if (ucdApplication) {
				importAction = UcdImportActionEnum.UPGRADE
			} else {
				importAction = UcdImportActionEnum.IMPORT
			}

			// Fix HTTP-type property definition problem that very between UCD versions.
			ucdApplicationImport.fixProcessHttpPropDefs(ucdSession)

			logInfo("$importAction Application [$application] into [" + ucdSession.getUcdUrl() + "]")
			String jsonStr = ucdApplicationImport.toJsonString(removeCrypt)
			
			logInfo("jsonStr=\n$jsonStr")
			
			// Will always use UcdImportRule.FAIL_IF_DOESNT_EXIST for options here so that this import only affects the application,
			// not the components, templates, or processes.
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/deploy/application/{importAction}")
				.resolveTemplate("importAction", importAction.getValue())
				.queryParam("upgradeType", UcdImportTypeEnum.FAIL_IF_DOESNT_EXIST)
				.queryParam("compTempUpgradeType", UcdImportTypeEnum.FAIL_IF_DOESNT_EXIST)
				.queryParam("processUpgradeType", UcdImportTypeEnum.FAIL_IF_DOESNT_EXIST)
			logDebug("target=$target".toString())
			
			MultiPart multiPart = new MultiPart()
			multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)
			multiPart.bodyPart(new FormDataBodyPart("file", jsonStr, MediaType.APPLICATION_OCTET_STREAM_TYPE))
			Response response = target.request().post(Entity.entity(multiPart, multiPart.getMediaType()))
			if (response.getStatus() != 200) {
				String errorMsg = response.readEntity(String.class)
				logError("$errorMsg\n${errorMsg.replaceAll(/.*(Error importing.*?)&quot.*/, '$1')}")
				throw new UcdInvalidValueException("Status: ${response.getStatus()} Unable to import application. $target")
			}

			// Re-add the components from the saved list.
			for (UcdComponentImport savedComponent in savedComponents) {
				actionsRunner.runAction([
					action: UcdAddComponentToApplication.getSimpleName(),
					application: application,
					component: savedComponent.getName()
				])
			}
		}
	}
}
