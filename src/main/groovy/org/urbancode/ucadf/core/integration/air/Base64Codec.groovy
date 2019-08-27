/**
 * This class provides a wrapper around the actual class that's not available until run time.
 */
package org.urbancode.ucadf.core.integration.air

import groovy.util.logging.Slf4j

@Slf4j
class Base64Codec {
	// Constructor.
	Base64Codec() {
	}
	
	// Decode a string.
	public byte[] decodeFromString(final String secretVar) {
		log.debug("decodeFromString secretVar=$secretVar")
		
		// The decoder class is available at runtime from the agent installation directory in securedata.jar.
		Class base64CodecClass = Base64Codec.class.classLoader.loadClass("com.urbancode.air.securedata.Base64Codec")
		log.debug("decodeFromString base64CodecClass=$base64CodecClass")
		
		// Get an instance of the class with the default constructor.
		Object base64CodecInstance = base64CodecClass.newInstance()
		log.debug("decodeFromString base64CodecInstance=$base64CodecInstance")
		
		// Return the decoded string as bytes.
		return base64CodecInstance.decodeFromString(secretVar)
	}
}
