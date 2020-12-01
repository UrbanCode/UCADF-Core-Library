/**
 * This action decrypts a value.
 * If the secret key is in a Java keystore then provide: keystore, keystoreType (optional), keystorePass, keyAlias, and keypass.
 * The value to decrypt may be in one of the following formats:
 *    crypt_v1{transformation|keyAlias|encodedIV|value}
 *    transformation|keyAlias|encodedIV|value
 *    keyAlias|encodedIV|value
 *    encodedIV|value
 *    value
 *  Other action properties may need to be provided depending on what value format is used.
 */
package org.urbancode.ucadf.core.action.integration.crypt

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.integration.crypt.UcAdfCrypt
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString

import groovy.util.logging.Slf4j

@Slf4j
class UcAdfDecrypt extends UcAdfAction {
	// Action properties.
	/** The Java keystore file name. */
	File keystore
	
	/** The Java keystore type. (Default is JCEKS.) */
	String keystoreType = UcAdfCrypt.DEFAULT_KEYSTORETYPE
	
	/** The Java keystore password. */
	UcAdfSecureString keystorePass
	
	/** The Jave keystore key alias. */
	String keyAlias
	
	/** The Jave keystore key alias password. */
	UcAdfSecureString keyPass
	
	/** Encoded IV (if IV is not part of value string). */
	UcAdfSecureString encodedIv

	/** Encoded key value (if no keystore provided). */
	UcAdfSecureString encodedKey

	/** Key algorithm. (Default is AES) */
	String keyAlgorithm = UcAdfCrypt.DEFAULT_KEYALGORITHM
	
	/** Cipher transformation. (Default is AES/CBC/PKCS5Padding) */
	String cipherTransformation = UcAdfCrypt.DEFAULT_CIPHERTRANSFORMATION

	/** The value to decrypt. */
	UcAdfSecureString value

	/**
	 * Runs the action.	
	 * @return Returns the decrypted value.
	 */
	public UcAdfSecureString run() {
		// Validate the action properties.
		validatePropsExistExclude([ 'keystore', 'keyAlias', 'keystorePass', 'keyPass', 'encodedIv', 'encodedKey' ])

		// Create a crypt object.
		UcAdfCrypt ucAdfCrypt = new UcAdfCrypt()

		// Set the crypt keystore information.
		ucAdfCrypt.setKeystore(keystore)
		ucAdfCrypt.setKeystoreType(keystoreType)
		ucAdfCrypt.setKeystorePass(keystorePass?.toString())
		ucAdfCrypt.setKeyAlias(keyAlias)
		ucAdfCrypt.setKeyPass(keyPass?.toString())
		ucAdfCrypt.setCipherTransformation(cipherTransformation)

		// Set the encoded IV.
		ucAdfCrypt.setEncodedIv(encodedIv?.toString())
		
		// Set the key information.
		ucAdfCrypt.setKeyAlgorithm(keyAlgorithm)
		ucAdfCrypt.setEncodedKey(encodedKey?.toString())

		// Decrypt and return the value.
		String decryptedValue = ucAdfCrypt.decrypt(value.toString())
		
		return new UcAdfSecureString(decryptedValue)
	}
}
