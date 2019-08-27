/**
 * This class is serializing/deserializing import objects.
 * */
package org.urbancode.ucadf.core.model.ucd.importExport

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import groovy.util.logging.Slf4j

@Slf4j
class UcdExport extends UcdObject {
	/** The list of keystore names required by the import object. */
	List<String> keystoreNames = []
	
	/** The associated import object. */
	UcdImport ucdImport

	// Constructors.
	UcdExport() {
	}
	
	UcdExport(final UcdImport ucdImport) {
		setUcdImport(ucdImport)
	}
	
	/**
	 * Sets the valid of the associated import object and identifies the encryption keys required at import time.
	 * @param ucdImport The import object.
	 */
	public void setUcdImport(final UcdImport ucdImport) {
		this.ucdImport = ucdImport

		// Find the crypt keys in the export text.
		Set keystores = new HashSet()

		Pattern pattern = Pattern.compile(/.*crypt_v1\{.*Padding\|(.*?)\|.*\}.*/)
		Matcher matcher = pattern.matcher(ucdImport.toJsonString())
		while (matcher.find()) {
			keystores.add(matcher.group(1))
		}
		
		log.info "Keystore names found in export text [${keystores.iterator().join(",")}]."

		// Set the keystore names from the text.
		this.keystoreNames = new ArrayList(keystores)
	}	
}
