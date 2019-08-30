/**
 * This action creates an authentication token.
 */
package org.urbancode.ucadf.core.action.ucd.authToken

import java.text.SimpleDateFormat

import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.action.ucd.user.UcdGetUser
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authToken.UcdAuthToken
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

import groovy.json.JsonBuilder

class UcdCreateAuthToken extends UcAdfAction {
	/** The user name or ID. */
    String user
	
	/** (Optional) The expiration date. Format: MM-dd-yyyy-HH:mm. Default is current date + 20 years. */
	String expireDate = ""
	
	/** (Optional) The description. */
	String description = ""
	
	/** (Optional) The allowed IPs. */
	String allowedIps = ""
	
	/** If true then skip if the user already has a token. Default it true. */
	Boolean skipIfUserHasToken = true
	
	/** The flag that indicates fail if the user already has a token. Default is true. */
	Boolean failIfExists = true

	/**
	 * Runs the action.	
	 * @return The information about the token creation.
	 */
	@Override
	public TokenReturn run() {
		// Validate the action properties.
		validatePropsExist()

		// Initialize the return object.
		TokenReturn tokenReturn = new TokenReturn()
		tokenReturn.setCreated(false)
		tokenReturn.setToken("")

		// Validate the user exists.
		UcdUser ucdUser = actionsRunner.runAction([
			action: UcdGetUser.getSimpleName(),
			actionInfo: false,
			user: user,
			failIfNotFound: true
		])

		// If optionally skipping, then determine if user already has tokens.
		List<UcdAuthToken> ucdAuthTokens = []
		if (skipIfUserHasToken || failIfExists) {
			ucdAuthTokens = actionsRunner.runAction([
				action: UcdGetAuthTokens.getSimpleName(),
				actionInfo: false,
				user: user
			])
		}
		
        // Create a user token if there aren't already tokens for the user.
        if ((skipIfUserHasToken || failIfExists) && ucdAuthTokens.size() > 0) {
			if (failIfExists) {
				throw new UcdInvalidValueException("Tokens already exist for user [$user].")
			} else {
				logInfo("Tokens already exist for user [$user].")
			}
        } else {
			// If no expire date is provided then it will be set to the current date + 20 years
			Date parsedDate
			if (!expireDate) {
				Calendar calendar = Calendar.getInstance()
				calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 20)
				parsedDate = calendar.getTime()
			} else {
				parsedDate = new SimpleDateFormat("MM-dd-yyyy-HH:mm").parse(expireDate)
			}
			
			SimpleDateFormat zDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
			String zExpireDate = zDateFormat.format(parsedDate)
			String expiration = parsedDate.getTime()
			
			String derivedDescription = description
			derivedDescription += (description ? " " : "") + "Created:" + zDateFormat.format(new Date())
			
			logInfo("Creating authentication token for user [$user] expireDate [$zExpireDate] description [$derivedDescription]")
			
			WebTarget target = ucdSession.getUcdWebTarget().path("/security/authtoken")
			logDebug("target=$target")
			
			Map requestMap = [ 
				userId: ucdUser.getId(), 
				expDate: zExpireDate, 
				expTime: "1970-01-02T05:30:00.000Z", 
				description: derivedDescription, 
				host: allowedIps, 
				expiration: expiration
			]
			
			JsonBuilder jsonBuilder = new JsonBuilder(requestMap)
			
			Response response = target.request(MediaType.APPLICATION_JSON).put(Entity.json(jsonBuilder.toString()))
			if (response.getStatus() == 200) {
				Properties responseProps = response.readEntity(Properties.class)
				tokenReturn.setToken(responseProps.get("token"))
			
				logInfo("User [$user] authentication token created.")
			} else {
				throw new UcdInvalidValueException(response)
			}
			
			tokenReturn.setCreated(true)
        }
		
		return tokenReturn
	}	

	/** This class returns information about a token creation. */
	static public class TokenReturn {
		/** If true then the token was created. */
		Boolean created = false
		
		/** The token value. This is the ONLY time you will be able to get the value of the token. */
		String token = ""
	}
}
