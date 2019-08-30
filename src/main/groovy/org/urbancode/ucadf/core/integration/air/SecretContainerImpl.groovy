/**
 * This class provides a wrapper around the actual class that's not available until run time.
 */
package org.urbancode.ucadf.core.integration.air

import java.lang.reflect.Constructor

class SecretContainerImpl {
	// The instantiated secret container.
	public Object secretContainerInstance
	
	// Constructor that instantiates an object using the real class.
	public SecretContainerImpl(byte[] secretBytes) {
		// Get the class.
        Class secretContainerClass = SecretContainerImpl.class.classLoader.loadClass("com.urbancode.air.securedata.SecretContainerImpl")
		
		// Get the constructor for the class.
        Constructor secretContainerConstructor = secretContainerClass.getConstructor(byte[].class)
		
		// Get a secret container instance.
        secretContainerInstance = secretContainerConstructor.newInstance([secretBytes] as Object[])
	}

	// Return the real secret container instance.	
	public Object getSecretContainerInstance() {
		return secretContainerInstance
	}
}
