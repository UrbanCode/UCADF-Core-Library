/**
 * This class is a fake trust manager.
 */
package org.urbancode.ucadf.core.integration.trustmanager

import java.security.cert.X509Certificate

import javax.net.ssl.X509TrustManager
	
class FakeX509TrustManager implements X509TrustManager {
	public X509Certificate[] getAcceptedIssuers() {
		return null
	}

	@Override
	public void checkClientTrusted(X509Certificate[] certs, String authType) {
		// Trust always
	}

	@Override
	public void checkServerTrusted(X509Certificate[] certs, String authType) {
		// Trust always
	}
}
