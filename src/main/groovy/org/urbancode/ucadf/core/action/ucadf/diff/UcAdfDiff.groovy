/**
 * This action runs a diff between two sets of UrbanCode entities, shows the differences, and optionally fails if a diff condition is not met.
 */
package org.urbancode.ucadf.core.action.ucadf.diff

import org.urbancode.ucadf.core.action.ucd.applicationProcess.UcdGetApplicationProcesses
import org.urbancode.ucadf.core.action.ucd.component.UcdGetComponentProperties
import org.urbancode.ucadf.core.action.ucd.componentProcess.UcdGetComponentProcesses
import org.urbancode.ucadf.core.action.ucd.componentTemplateProcess.UcdGetComponentTemplateProcesses
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcess.UcdApplicationProcess
import org.urbancode.ucadf.core.model.ucd.componentProcess.UcdComponentProcess
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdSecureString
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.system.UcdSession

class UcAdfDiff extends UcAdfAction {
	// Action properties.
	/** The type of entities to compare. */
	UcAdfDiffTypeEnum type
	
	/** The entity name to compare from. */
	String name1
	
	/** The entity name to compare to. */
	String name2
	
	/** The diff failure conditions.
	 * Use < to fail if name1 has entries not in name2 and use > to fail if name2 has entries not in name1. Use <> for both.
	 */
	String failConditions = ""
	
	/** By default the same {@link #ucdSession} is used to compare to but a different instance may be specified.
	 * (Optional) UCD URL.
	 */
	URL toUcdUrl
	
	/** (Optional) UCD user ID. */
	String toUcdUserId
	
	/** (Optional) UCD user password. */
	UcdSecureString toUcdUserPw
   
	// Private properties.
	private UcdSession ucdSession1
	private UcdSession ucdSession2

	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExistExclude(
			[
				'toUcdUrl',
				'toUcdUserId',
				'toUcdUserPw'
			]
		)

		// The first session is the runner session.
		ucdSession1 = ucdSession

		// The second session defaults to the first session.
		ucdSession2 = ucdSession
		if (toUcdUrl) {
			ucdSession2 = new UcdSession(
				toUcdUrl, 
				toUcdUserId, 
				toUcdUserPw as String
			)
		}
		
		switch (type) {
			case UcAdfDiffTypeEnum.ApplicationProcesses:
				diffApplicationProcesses()
				break
				
			case UcAdfDiffTypeEnum.ComponentProcesses:
				diffComponentProcesses()
				break
				
			case UcAdfDiffTypeEnum.ComponentProperties:
				diffComponentProperties()
				break
	
				
			case UcAdfDiffTypeEnum.ComponentTemplateProcesses:
				diffComponentTemplateProcesses()
				break
				
			case UcAdfDiffTypeEnum.ComponentTemplateProperties:
				diffComponentTemplateProperties()
				break
				
			default:
				throw new UcdInvalidValueException("Don't know how to process diff type [$type].")
		}		
	}
	
	// Find the differences between application process names in two different applications.
	private diffApplicationProcesses() {
		logVerbose("Finding differences between application [$name1] and [$name2] processes.")
		
		List<UcdApplicationProcess> appProcesses1 = actionsRunner.runAction([
			action: UcdGetApplicationProcesses.getSimpleName(),
			application: name1,
			full: true
		])

		List<UcdApplicationProcess> appProcesses2 = actionsRunner.runAction([
			action: UcdGetApplicationProcesses.getSimpleName(),
			application: name2,
			full: true
		])
		
		diffSets(
			"${ucdSession1.getUcdUrl()} $name1 processes", 
			appProcesses1.collect { it.getName() } as Set, 
			"${ucdSession2.getUcdUrl()} $name2 processes", 
			appProcesses2.collect { it.getName() } as Set
		)
	}
	
	 // Find the differences between component process names in two different components.
	private diffComponentProcesses() {
		logVerbose("Finding differences beteween component [$name1] and [$name2] processes.")
		
		List<UcdComponentProcess> compProcesses1 = actionsRunner.runAction([
			action: UcdGetComponentProcesses.getSimpleName(),
			component: name1
		])
		
		List<UcdComponentProcess> compProcesses2 = actionsRunner.runAction([
			action: UcdGetComponentProcesses.getSimpleName(),
			component: name2
		])

		diffSets(
			"${ucdSession1.getUcdUrl()} $name1 processes",
			compProcesses1.collect { it.getName() } as Set,
			"${ucdSession2.getUcdUrl()} $name2 processes",
			compProcesses2.collect { it.getName() } as Set
		)
	}

	 // Find the differences between component property names in two different components.
	private diffComponentProperties() {
		logVerbose("Finding differences between component [$name1] and [$name2] properties.")
		
		List<UcdProperty> compProperties1 = actionsRunner.runAction([
			action: UcdGetComponentProperties.getSimpleName(),
			component: name1,
			excludeInherited: true
		])
		
		List<UcdProperty> compProperties2 = actionsRunner.runAction([
			action: UcdGetComponentProperties.getSimpleName(),
			component: name2,
			excludeInherited: true
		])

		diffSets(
			"${ucdSession1.getUcdUrl()} $name1 properties",
			compProperties1.collect { it.getName() } as Set,
			"${ucdSession2.getUcdUrl()} $name2 properties",
			compProperties2.collect { it.getName() } as Set
		)
	}
	
	 // Find the differences between component template process names in two different component templates.
	private diffComponentTemplateProcesses() {
		logVerbose("Finding differences between component template [$name1] and [$name2] processes.")
		
		List<UcdComponentProcess> compTemplateProcesses1 = actionsRunner.runAction([
			action: UcdGetComponentTemplateProcesses.getSimpleName(),
			componentTemplate: name1
		])
		
		List<UcdComponentProcess> compTemplateProcesses2 = actionsRunner.runAction([
			action: UcdGetComponentTemplateProcesses.getSimpleName(),
			componentTemplate: name2
		])

		diffSets(
			"${ucdSession1.getUcdUrl()} $name1 processes",
			compTemplateProcesses1.collect { it.getName() } as Set,
			"${ucdSession2.getUcdUrl()} $name2 processes",
			compTemplateProcesses2.collect { it.getName() } as Set
		)
	}

	 // Find the differences between component template property names in two different component templates.
	private diffComponentTemplateProperties() {
		logVerbose("Finding differences between component template [$name1] and [$name2] properties.")
		
		List<UcdProperty> compTemplateProperties1 = actionsRunner.runAction([
			action: UcdGetComponentProperties.getSimpleName(),
			componentTemplate: name1
		])
		
		List<UcdProperty> compTemplateProperties2 = actionsRunner.runAction([
			action: UcdGetComponentProperties.getSimpleName(),
			componentTemplate: name2
		])

		diffSets(
			"${ucdSession1.getUcdUrl()} $name1 properties",
			compTemplateProperties1.collect { it.getName() } as Set,
			"${ucdSession2.getUcdUrl()} $name2 properties",
			compTemplateProperties2.collect { it.getName() } as Set
		)
	}
	
	// Find the differences between two sets.
	private diffSets(
		final String set1Title, 
		final Set set1, 
		final String set2Title, 
		final Set set2) {
		
		println "===== [$set1Title] Compared To [$set2Title] ====="
		// Get the differences between the sets.
		Set diff1 = (set1 - set2)
		Set diff2 = (set2 - set1)
		
		if (diff1.size() > 0) {
			println "< In [$set1Title] and not in [$set2Title]."
			diff1.each { println "[$it]" }
			if (failConditions.contains("<")) {
				throw new UcdInvalidValueException("[$set1Title] has entries not in [$set2Title].")
			}
		}
		
		if (diff2.size() > 0) {
			println "> In [$set2Title] and not in [$set1Title]."
			diff2.each { println "[$it]" }
			if (failConditions.contains(">")) {
				throw new UcdInvalidValueException("[$set2Title] has entries not in [$set1Title].")
			}
		}
	}
}
