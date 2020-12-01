/**
 * This action generates a secret key.
 */
package org.urbancode.ucadf.core.action.integration.crypt

import java.security.Key

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.integration.crypt.UcAdfCrypt
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString

import groovy.util.logging.Slf4j

@Slf4j
class UcAdfGenerateKey extends UcAdfAction {
	// Action properties.
	/** The key generation password. */
	UcAdfSecureString password
	
	/** The key generation salt. */
	UcAdfSecureString salt
	
	/** The key generation algorithm. */
	String algorithm = UcAdfCrypt.DEFAULT_KEYGEN_ALGORITHM

	/**
	 * Runs the action.	
	 * @return Returns the generated key.
	 */
	public UcAdfSecureString run() {
		// Validate the action properties.
		validatePropsExist()

		UcAdfCrypt sfUcAdfCrypt = new UcAdfCrypt()
		
		Key secretKey = sfUcAdfCrypt.generateKey(
			password.toString(),
			salt.toString(),
			algorithm
		)
		
		// Return a base 64 encoded string.
		return new UcAdfSecureString(sfUcAdfCrypt.getEncodedKey(secretKey))
	}
}
