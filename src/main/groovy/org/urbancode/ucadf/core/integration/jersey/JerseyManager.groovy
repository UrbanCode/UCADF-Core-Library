/**
 * This class is the Jersey manager.
 */
package org.urbancode.ucadf.core.integration.jersey

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder

import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.HttpUrlConnectorProvider
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature
import org.glassfish.jersey.filter.LoggingFilter
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.urbancode.ucadf.core.integration.trustmanager.FakeX509TrustManager

import groovy.util.logging.Slf4j

@Slf4j
class JerseyManager {
	// The patch request type.
	public final static String PATCH = "PATCH"
	
	/**
	 * Get a configured client.
	 * @param config The configuration.
	 * @return The client.
	 */
	public static Client getConfiguredClient(final ClientConfig config) {
		return getConfiguredClient(
			null, 
			null, 
			config
		)
	}
	
	/**
	 * Get a configured client.
	 * @param userId The user ID.
	 * @param userPw The user password.
	 * @param config The configuration.
	 * @return The configured client.
	 */
	public static Client getConfiguredClient(
		final String userId = null, 
		final String userPw = null, 
		final ClientConfig config = null) {
		
		// Create a trust manager that does not validate certificate chains.
		TrustManager[] trustAllCerts = [new FakeX509TrustManager()]
		
		// Force a high TLS level.
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2")
		
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom())
		
		ClientBuilder builder = ClientBuilder.newBuilder().sslContext(sslContext)
		if (config) {
			builder.withConfig(config)
		}
		
		// Create a Jersey client
		Client client = builder.build()
		
		// Set the HTTP authentication feature for the client
		if (userId && userPw) {
			HttpAuthenticationFeature basicAuthFeature = HttpAuthenticationFeature.basic(userId as String, userPw as String)
			client.register(basicAuthFeature)
		}

		client.register(new LoggingFilter(new JerseyLogger(), true)).register(MultiPartFeature.class).register(JacksonFeature.class);

		// This allows for non-standard request methods such as PATCH.
		client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)

		return client
	}
}
