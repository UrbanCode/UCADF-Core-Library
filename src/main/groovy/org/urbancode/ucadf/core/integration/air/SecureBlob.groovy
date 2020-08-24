/**
 * This class provides a wrapper around the actual class that's not available until run time.
 */
package org.urbancode.ucadf.core.integration.air

import groovy.util.logging.Slf4j

import java.lang.reflect.Method

@Slf4j
class SecureBlob {
	private Object secureBlobInstance
	
	// Constructor.
	SecureBlob(Object secureBlobInstance) {
		this.secureBlobInstance = secureBlobInstance
	}

	// Get a secure blob from encrypted bytes.	
	public static SecureBlob fromEncryptedBytes(
		final SecretContainerImpl secretContainer, 
		final InputStream inputStream) {
		
		// Class is available at runtime from the agent installation directory in CommonsFileUtils.jar.
		Class ioClass = SecureBlob.class.classLoader.loadClass("com.urbancode.commons.util.IO")
		log.debug("fromEncryptedBytes ioClass=$ioClass")
		
		// Class is available at runtime from the agent installation directory in securedata.jar.
		Class secureBlobClass = SecureBlob.class.classLoader.loadClass("com.urbancode.air.securedata.SecureBlob")
		log.debug("fromEncryptedBytes secureBlobClass=$secureBlobClass")
		
		// Class is available at runtime from the agent installation directory in securedata.jar.
		Class secretContainerClass = SecureBlob.class.classLoader.loadClass("com.urbancode.air.securedata.SecretContainer")
		log.debug("fromEncryptedBytes secretContainerClass=$secretContainerClass")
		
		// Get the class methods.		
		Method ioMethod = ioClass.getMethod("read", InputStream.class)
		log.debug("fromEncryptedBytes ioMethod=$ioMethod")
		Method secretContainerMethod = secureBlobClass.getMethod("fromEncryptedBytes", secretContainerClass, byte[].class)
		log.debug("fromEncryptedBytes secretContainerMethod=$secretContainerMethod")
		
		// Return a secure blob object.		
		return new SecureBlob(
			secretContainerMethod.invoke(
				null, 
				secretContainer.getSecretContainerInstance(), 
				ioMethod.invoke(null, inputStream)
			)
		)
	}

	// A wrapper around the real class get bytes method.		
	public Object get() {
		return secureBlobInstance.get()
	}
	
	// Get a secure blob from unencrypted bytes.	
	public static SecureBlob fromUnencryptedBytes(
		final SecretContainerImpl secretContainer,
		final byte[] bytes) {
		
		Class secureBlobClass = SecureBlob.class.classLoader.loadClass("com.urbancode.air.securedata.SecureBlob")
		log.debug("fromUnencryptedBytes secureBlobClass=$secureBlobClass")
		Class secretContainerClass = SecureBlob.class.classLoader.loadClass("com.urbancode.air.securedata.SecretContainer")
		log.debug("fromUnencryptedBytes secretContainerClass=$secretContainerClass")
		Method secretContainerMethod = secureBlobClass.getMethod("fromUnencryptedBytes", secretContainerClass, byte[].class)
		log.debug("fromUnencryptedBytes secretContainerMethod=$secretContainerMethod")
		
		return new SecureBlob(
			secretContainerMethod.invoke(
				null, 
				secretContainer.getSecretContainerInstance(), 
				bytes
			)
		)
	}

	// A wrapper around the real class get encrypted bytes method.		
	public Object getEncryptedBytes() {
		secureBlobInstance.getEncryptedBytes()
	}
}
