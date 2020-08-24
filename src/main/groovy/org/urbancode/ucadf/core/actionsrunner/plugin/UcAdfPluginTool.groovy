package org.urbancode.ucadf.core.actionsrunner.plugin

import org.urbancode.ucadf.core.integration.air.Base64Codec
import org.urbancode.ucadf.core.integration.air.SecretContainerImpl
import org.urbancode.ucadf.core.integration.air.SecureBlob

import groovy.util.logging.Slf4j

// Supports functionality needed by plugins.
@Slf4j
public class UcAdfPluginTool {
	static final String ENVVARNAME_SECRET = "ucd.properties.secret"
	
	private SecretContainerImpl secretContainer
		
	// Constructor.	
	UcAdfPluginTool() {
	}

	// Get the step properties from the encrypted input properties file.
	// If secretVar is provided then that is used for decryption, otherwise secretVar comes from STDIN.
	public getStepProperties(
		final String inPropsFileName,
		final String secretVar = "") {
		
		// Get the secret container to use for decryption.		
		SecretContainerImpl secretContainer = getSecretContainer(secretVar)

		Properties inProps = new Properties()
		InputStream inPropsStream
		try {
			// Open the input properties file stream.
			log.debug("getStepProperties inPropsFileName=$inPropsFileName")
			inPropsStream = new FileInputStream(inPropsFileName)
			
			// Read the encrypted file contents and decrypt into a stream.
			SecureBlob secureBlob = SecureBlob.fromEncryptedBytes(
				secretContainer,
				inPropsStream
			)
	
			// Load the properties from the decrypted stream.
			inProps.load(
				new ByteArrayInputStream(secureBlob.get())
			)
		} finally {
			close(inPropsStream)
		}

		return inProps
	}
	
	// Write the output properties file.
	public writeOutputProperties(
		final Properties outProps, 
		final String outPropsFileName) {
		
		// Get the secret container to use for encryption.
		SecretContainerImpl secretContainer = getSecretContainer()
		
		// Serialize the output properties to an output stream.
		ByteArrayOutputStream bytesOutStream
		try {
			bytesOutStream = new ByteArrayOutputStream()
			outProps.store(bytesOutStream, "")

			// Encrypt the output stream bytes and write to the output properties file.
			OutputStream outPropsStream 
			try {
				outPropsStream = new FileOutputStream(outPropsFileName)
				SecureBlob secureBlob = SecureBlob.fromUnencryptedBytes(
					secretContainer, 
					bytesOutStream.toByteArray()
				)
				outPropsStream.write(secureBlob.getEncryptedBytes())
			} finally {
				close(outPropsStream)
			}
		} finally {
			close(bytesOutStream)
		}
	}
	
	// Get the secret container to use for encryption/decryption.
	public SecretContainerImpl getSecretContainer(String secretVar = "") {
		log.debug("getSecretContainer.")

		// If the secret container hasn't been created yet then create it.
		if (!this.secretContainer) {
			if (secretVar) {
				log.debug("getSecretContainer using secretVar from parameter.")
			} else {
				// Get the secret.
				log.debug("getSecretContainer get secretVar from STDIN.")
				Properties stdInProps = new Properties()
				stdInProps.load(System.in)
				secretVar = stdInProps.getProperty(ENVVARNAME_SECRET)
			}
			log.debug("getSecretContainer secretVar=$secretVar")
			
			// Decode the secret string to bytes.
			Base64Codec base64Codec = new Base64Codec()
			byte[] secretBytes = base64Codec.decodeFromString(secretVar)
			log.debug("getSecretContainer secretBytes [$secretBytes].")
		
			// Construct a secret container.
			this.secretContainer = new SecretContainerImpl(secretBytes)
		}
		
		return this.secretContainer
	}

	// Close an open stream.	
	private close(Closeable closeStream) {
		if (closeStream) {
			try {
				closeStream.close()
			} catch (Exception e) {
				// Ignore close exception.
			}
		}
	}
}
