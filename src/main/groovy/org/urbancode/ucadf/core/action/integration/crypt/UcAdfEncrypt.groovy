/**
 * This action encrypts a value.
 */
package org.urbancode.ucadf.core.action.integration.crypt

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.integration.crypt.UcAdfCrypt
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString

import groovy.util.logging.Slf4j

@Slf4j
class UcAdfEncrypt extends UcAdfAction {
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

	/** The value to encrypt. */
	UcAdfSecureString value

	/**
	 * Runs the action.	
	 * @return Returns the encoded IV and encrypted value.
	 */
	public UcAdfEncryptReturn run() {
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

		// Encrypt and return the value.
		String encryptedValue = ucAdfCrypt.encrypt(value.toString())

		UcAdfEncryptReturn encryptReturn = new UcAdfEncryptReturn()
		encryptReturn.setEncodedIv(new UcAdfSecureString(ucAdfCrypt.getEncodedIv()))
		encryptReturn.setValue(new UcAdfSecureString(encryptedValue))
		
		return encryptReturn
	}
}

class UcAdfEncryptReturn {
	UcAdfSecureString encodedIv
	UcAdfSecureString value
}
