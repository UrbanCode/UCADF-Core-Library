/**
 * This class represents a UCD session.
 */
package org.urbancode.ucadf.core.model.ucd.system

import java.util.logging.Filter
import java.util.logging.LogRecord
import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.ws.rs.client.Client
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.ClientProperties
import org.urbancode.ucadf.core.integration.jersey.JerseyManager
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException

import com.fasterxml.jackson.annotation.JsonIgnore

import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

@Slf4j
@TypeChecked
class UcdSession {
	// UrbanCode version matches.
    public final static String UCDVERSION_61 = /6\.1\..*/
    public final static String UCDVERSION_62 = /6\.2\..*/
	public final static String UCDVERSION_70 = /7\.0\..*/

	// Session properties.	
	public final static String PROPUCDURL = "ucdUrl"
	public final static String PROPUCDUSERID = "ucdUserId"
	public final static String PROPUCDUSERPW = "ucdUserPw"
	public final static String PROPUCDAUTHTOKEN = "ucdAuthToken"
	public final static String PROPUCDAHWEBURL = "AH_WEB_URL"
	public final static String PROPUCDDSAUTHTOKEN = "DS_AUTH_TOKEN"

	/** The UCD instance URL. */
	private String ucdUrl
	
	/** The UCD instance user ID. */
	private String ucdUserId
	
	/** The UCD instance user password. */
	private String ucdUserPw
	
	/** The HTTP authentication string. */
	private String ucdHttpAuthStr
	
	/** The UCD instance version. */
	public String ucdVersion
	
	/** The UCD web client. */	
	private Client ucdWebClient
	
	/** The UCD non-compliant web client. */
	private Client ucdWebNonCompliantClient

	// Constructors.
	public UcdSession() {
		this("", "", "")
	}
		
	public UcdSession(final Properties props) {
		this(
			props, 
			new ConfigObject()
		)
	}
	
	public UcdSession(
		final Properties props, 
		final ConfigObject config) {
		
		String ucdUrl = ""
		String ucdUserId = ""
		String ucdUserPwOrToken = ""
		if (config) {
			if (config.get(PROPUCDURL)) {
				ucdUrl = config.get(PROPUCDURL)
			}
			
			if (config.get("ucdUserId")) {
				ucdUserId = config.ucdUserId
			}
			
			if (config.get("ucdUserPw")) {
				ucdUserPwOrToken = config.ucdUserPw
			}
		}
		
		initializeSession(
			props, 
			ucdUrl, 
			ucdUserId, 
			ucdUserPwOrToken
		)
	}

	public UcdSession(
		final Properties props, 
		final String ucdUserId,
		final String ucdUserPwOrToken) {
		
		initializeSession(
			props, 
			ucdUserId, 
			ucdUserPwOrToken
		)
	}
	
	public UcdSession(
		final URL ucdUrl, 
		final String ucdUserId, 
		final String ucdUserPwOrToken) {
		
		Properties inProps = new Properties()
		inProps.put(UcdSession.PROPUCDAHWEBURL, ucdUrl.toString())
		inProps.put(UcdSession.PROPUCDDSAUTHTOKEN, "")

		initializeSession(
			inProps, 
			ucdUserId, 
			ucdUserPwOrToken
		)
	}
	
	public UcdSession(
		final String ucdUrl, 
		final String ucdUserId, 
		final String ucdUserPwOrToken) {
		
		Properties inProps = new Properties()
		inProps.put(UcdSession.PROPUCDAHWEBURL, ucdUrl)
		inProps.put(UcdSession.PROPUCDDSAUTHTOKEN, "")

		initializeSession(
			inProps, 
			ucdUserId, 
			ucdUserPwOrToken
		)
	}

	private initializeSession(
		final Properties props, 
		final String ucdUserId, 
		final String ucdUserPwOrToken) {
		
		initializeSession(
			props, 
			"", 
			ucdUserId, 
			ucdUserPwOrToken
		)
	}
	
	// Defaults to using URL and token values from the environment or properties.
	// A property is typically set for workstation unit testing when it's not available as an environment variable.
	private initializeSession(
		final Properties props, 
		final String ucdUrl, 
		final String ucdUserId, 
		final String ucdUserPwOrToken) {
		
		final def env = System.getenv()

		String derivedUcdUrl = ucdUrl
		if (!derivedUcdUrl) {
			if (props[PROPUCDAHWEBURL]) {
				derivedUcdUrl = props[PROPUCDAHWEBURL]
			} else {
				derivedUcdUrl = env[PROPUCDAHWEBURL]
			}
		}
		this.ucdUrl = derivedUcdUrl
		
		if (ucdUserId && ucdUserPwOrToken) {
			// If user ID and password provided then use that for BASIC authentication
			log.debug "Using provided ucdUserId and ucdUserPwOrToken"
			ucdHttpAuthStr = "$ucdUserId:$ucdUserPwOrToken"
			this.ucdUserId = ucdUserId
			this.ucdUserPw = ucdUserPwOrToken
		} else if (!ucdUserId && ucdUserPwOrToken) {
			// If no user ID provided and a password was provided then assume the password id a token
			log.debug "Using provided ucdUserPwOrToken"
			ucdHttpAuthStr = /PasswordIsAuthToken:{"token":"$ucdUserPwOrToken"}/
			this.ucdUserId = "PasswordIsAuthToken"
			this.ucdUserPw = ucdUserPwOrToken
		} else {
			// If no user ID provided and no password provided then use the session token
			String ucdAuthToken = env[PROPUCDDSAUTHTOKEN]
			if (ucdAuthToken) {
				log.debug "Using session token from $PROPUCDDSAUTHTOKEN environment variable."
			} else {
				log.debug "Using session token from $PROPUCDDSAUTHTOKEN property."
				ucdAuthToken = props[PROPUCDDSAUTHTOKEN]
			}
		
			ucdHttpAuthStr = /PasswordIsAuthToken:{"token":"$ucdAuthToken"}/
			this.ucdUserId = "PasswordIsAuthToken"
			this.ucdUserPw = ucdAuthToken
		}
	}
	
	public String getUcdUrl() {
		return ucdUrl
	}
	
	public void setUcdUrl(final String ucdUrl) {
		this.ucdUrl = ucdUrl
	}

	public String getUcdInstanceName() {
		return new URL(ucdUrl).getHost().split(/\./)[0]
	}

	public String getUcdInstanceId() {
		return getUcdInstanceName().replaceAll(/.*?(\d+)$/, '$1')
	}
	
	public String getUcdHttpAuthStr() {
		return ucdHttpAuthStr
	}
	
	public void setUcdHttpAuthStr(final String ucdHttpAuthStr) {
		this.ucdHttpAuthStr = ucdHttpAuthStr
	}

	public String getUcdUserId() {
		return ucdUserId
	}
	
	public String getUcdUserPw() {
		return ucdUserPw
	}

	/**
	 * Run a shell command.
	 * @param commandList
	 * @param maxWaitSecs The maximum number of seconds to wait.
	 * @param showOutput The flag to indicate to show output.
	 * @param throwOnFail The flag to indicate throw an exception on failure.
	 * @param noDefaultAuth The flag to indicate no default authentication.
	 * @return The stdout buffer, the stderr buffer, the exit value.
	 */
	public static executeCommand(
		final List commandList, 
		final Integer maxWaitSecs, 
		final Boolean showOutput, 
		final Boolean throwOnFail, 
		final Boolean noDefaultAuth = false) {
		
		// Convert list to string array
		String[] commandArr = new String[commandList.size()]
		
		// Replace any null elements with empty string
		for (int i = 0; i < commandList.size(); i++) {
			if (commandList[i] == null) {
				commandArr[i] = ""
			} else {
				commandArr[i] = commandList[i]
			}
		}

		// Initiate the execution
		Process process
		if (noDefaultAuth) {
			// Get the current system environment variables and remove the AH_WEB_URL and DS_AUTH_TOKEN variables so that udclient commands will not pick them up
			Map envVars = System.getenv()
			List envVarsList = []
			envVars.each { k, v ->
				if (!(k ==~ "AH_WEB_URL" || k ==~ "DS_AUTH_TOKEN")) {
					envVarsList.add("$k=$v")
				}
			}
			process = Runtime.getRuntime().exec(commandArr, envVarsList as String[])
		} else {
			process = Runtime.getRuntime().exec(commandArr)
		}

		// Wait on the execution and get the results
		StringBuffer stdOut = new StringBuffer()
		StringBuffer stdErr = new StringBuffer()
		process.consumeProcessOutput(stdOut, stdErr)
		process.waitForOrKill(maxWaitSecs * 1000)
		def exitValue = process.exitValue()
		if (showOutput) {
			if (stdOut) {
				println "<<<STDOUT>>>\n$stdOut"
			}
			if (stdErr) {
				println "<<<STDERR>>>\n$stdErr"
			}
		}
		if (exitValue && throwOnFail) {
			throw new UcdInvalidValueException("Command failed.")
		}

		return [ stdOut, stdErr, exitValue]
	}
	
	/**
	 * Get a UCD web target.
	 * @return The UCD web target.
	 */
	public WebTarget getUcdWebTarget() {
		if (!getUcdUrl()) {
			throw new UcdInvalidValueException("Unable to determine value for ucdUrl.")
		}

		if (!ucdWebClient) {
			ucdWebClient = getUcdConfiguredClient()
		}

		return ucdWebClient.target(getUcdUrl())
	}
	
	/**
	 * Get a non-compliant web client.
	 * @return The non-compliant web client.
	 */
	public Client getWebNonCompliantClient() {
		if (!ucdWebNonCompliantClient) {
			ClientConfig config = new ClientConfig()
			config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true)
			ucdWebNonCompliantClient = getUcdConfiguredClient(config)

			// Suppress the DELETE with no body warnings
			java.util.logging.Logger jerseyLogger =
				java.util.logging.Logger.getLogger(org.glassfish.jersey.client.JerseyInvocation.class.getName())
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
		}
		return ucdWebNonCompliantClient
	}
	
	// Get a configured client.
	private Client getUcdConfiguredClient(final ClientConfig config = null) {
		Client client
		if (ucdUserId && ucdUserPw) {
			client = JerseyManager.getConfiguredClient(ucdUserId, ucdUserPw, config)
		} else {
			client = JerseyManager.getConfiguredClient(null, null, config)
		}
		
		return client
	}
	
	/**
	 * Get the UCD version.
	 * @return The UCD version.
	 */
	@JsonIgnore
	public String getUcdVersion() {
        if (!ucdVersion) {
			Client client = JerseyManager.getConfiguredClient()

			WebTarget target = client.target(getUcdUrl())
			
			// By adding this header it doesn't require authentication.
			Response response = target.request().get()
			if (response.getStatus() != 200 && response.status != 401) {	
				log.error response.readEntity(String.class)
				throw new UcdInvalidValueException("Status: ${response.getStatus()} Unable to get UrbanCode Deploy version. $target")
			}
			
			String responseText = response.readEntity(String.class)

			// Attempt different pattern matches to get the version from the returned text.
			String returnUcdVersion
			String VERSION_PATTERN1 = /"(?s).*"version %s", "(.*?)".*/
			String VERSION_PATTERN2 = /.* class="productVersion">(.*)<.*/
			for (patternStr in [ VERSION_PATTERN1, VERSION_PATTERN2 ]) {
				Pattern pattern = Pattern.compile(patternStr)
				Matcher matcher = pattern.matcher(responseText)
				if (matcher.find()) {
					returnUcdVersion = matcher.group(1)
					break
				}
			}
			
            ucdVersion = returnUcdVersion.replaceAll("v. ", "")
			
            log.info "UCD Version [$ucdVersion]."
        }

        return ucdVersion
	}

	/**
	 * Determine if the UCD instance is using the specified version.
	 * @param versionRegex The version regular expression.
	 * @return True if the version matches.
	 */
	@JsonIgnore
	public isUcdVersion(final String versionRegex) {
        return (getUcdVersion() ==~ /$versionRegex/)
	}
}
