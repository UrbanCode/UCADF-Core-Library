/**
 * This class is the Jersey manager.
 */
package org.urbancode.ucadf.core.integration.jersey

import java.util.logging.Filter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.ws.rs.client.Client
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.Feature

import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.HttpUrlConnectorProvider
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.logging.LoggingFeature
import org.glassfish.jersey.logging.LoggingFeature.Verbosity
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
		final ClientConfig config = null,
		final Boolean trustAllCerts = true) {
		
		// Force a high TLS level.
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2")
		
		// Create a trust manager that does not validate certificate chains.
		TrustManager[] fakeTrustManagers = [ new FakeX509TrustManager() ]
		
		if (trustAllCerts) {
			sslContext.init(null, fakeTrustManagers, new java.security.SecureRandom())
	
			// Set an all-trusting host name verifier.
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true
				}
			}
	
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
		}
		
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

		client.register(MultiPartFeature.class).register(JacksonFeature.class)

		// Add a verbose logger for debugging purposes.
		if (false) {
			Logger logger = Logger.getLogger(getClass().getName())
			Feature loggingFeature = new LoggingFeature(logger, Level.INFO, Verbosity.PAYLOAD_ANY, null)
			client.register(loggingFeature)
		}

		// This allows for non-standard request methods such as PATCH.
		client.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)

		return client
	}
	
	/**
	 * Get a non-compliant configured client.
	 * @return The non-compliant configured client.
	 */
	public static Client getNonCompliantConfiguredClient(
		final String userId = null, 
		final String userPw = null, 
		final ClientConfig config = new ClientConfig()) {
		
		config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
		Client client = getConfiguredClient(userId, userPw, config)

		// Suppress the DELETE with no body warnings
		java.util.logging.Logger jerseyLogger = java.util.logging.Logger.getLogger(org.glassfish.jersey.client.JerseyInvocation.class.getName())
		
		jerseyLogger.setFilter(new Filter() {
			@Override
			public boolean isLoggable(LogRecord record) {
				boolean isLoggable = true
				if (record.getMessage().contains("Entity must be null for http method DELETE")) {
					isLoggable = false
				}
				return isLoggable
			}
		})
			
		return client
	}
}
