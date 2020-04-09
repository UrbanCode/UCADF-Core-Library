/**
 * This class is the superclass for the classes that deserialize import objects.
 */
package org.urbancode.ucadf.core.model.ucd.importExport

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.genericProcess.UcdGenericProcessImport

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.util.logging.Slf4j

@Slf4j
abstract class UcdImport extends UcdObject {
	// Constructors.	
	UcdImport() {
	}
	
	/**
	 * Gets a JSON representation of the object.
	 * @param removeCrypt Flag that indicates to remove encryption strings.
	 * @return The JSON string.
	 */
	@JsonIgnore
	public String toJsonString(final Boolean removeCrypt) {
		// Serialize this object to a JSON string.
		String jsonStr = new ObjectMapper().writeValueAsString(this)

		// Optionally remove any encryption strings.
		log.debug "Removing encryption strings."
		if (removeCrypt) {
			jsonStr = jsonStr.replaceAll(/"crypt_v1\{.*?\}"/, /""/)
		}
		
		// TODO: Correct for an idiosyncrasy/bug(?) where JSON ends up with "\"\"" instead of ""  
		// Was able to remove this because it seems that toPretty was causing the extra quotes issue
		// jsonStr = jsonStr.replaceAll(/"\\"\\""/, /""/)
		
		return jsonStr
	}

	/**
	 * Get a map of generic process import objects by getting specific ones from this import object.
	 * @param genericProcesse The list of generic processes.
	 * @param match The regular expression used to match the generic process names to return.
	 * @return The map of generic process improt objects.
	 */
	public static Map<String, UcdGenericProcessImport> getGenericProcessImports(
		final List<UcdGenericProcessImport> genericProcesses, 
		final String match = "") {
		
		Map<String, UcdImport> genProcessImportMap = new TreeMap()

		// Process each of the generic processes used by the entity
		for (UcdGenericProcessImport genProcessImport in genericProcesses) {
			if (genProcessImport.getName() ==~ match) {
				genProcessImportMap.put(genProcessImport.getName(), genProcessImport)
			}
		}
		
		return genProcessImportMap
	}
}
