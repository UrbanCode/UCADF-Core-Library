/**
 * This action gets an notification scheme.
 */
package org.urbancode.ucadf.core.action.ucd.notificationScheme

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.notificationScheme.UcdNotificationScheme

class UcdGetNotificationScheme extends UcAdfAction {
	// Action properties.
	/** The notification scheme name or ID. */
	String notificationScheme
	
	/** The flag that indicates fail if the notification scheme is not found. Default is true. */
	Boolean failIfNotFound = true
	
	/**
	 * Runs the action.
	 * @return The notification scheme object.
	 */
	public UcdNotificationScheme run() {
		// Validate the action properties.
		validatePropsExist()

		logDebug("Getting notification scheme [$notificationScheme].")

		UcdNotificationScheme ucdNotificationScheme
		
		// If a notification scheme ID was provided then use it. Otherwise get the notification scheme information to get the ID.
		String notificationSchemeId
		if (UcdObject.isUUID(notificationScheme)) {
			notificationSchemeId = notificationScheme
		} else {
			// No API found to get a single notification scheme by name so have to get the list of all notification schemes then select the one from it.
			List<UcdNotificationScheme> ucdNotificationSchemes = actionsRunner.runAction([
				action: UcdGetNotificationSchemes.getSimpleName(),
				actionInfo: false
			])
	
			UcdNotificationScheme ucdFindNotificationScheme = ucdNotificationSchemes.find {
				(it.getName() == notificationScheme)
			}
		
			if (ucdFindNotificationScheme) {
				notificationSchemeId = ucdFindNotificationScheme.getId()
			} else {
				if (failIfNotFound) {
					throw new UcAdfInvalidValueException("Notification scheme [$notificationScheme] not found.")
				}
			}
		}
		
		// Get the notification scheme details.
		if (notificationSchemeId) {
			WebTarget target = ucdSession.getUcdWebTarget().path("/rest/notification/notificationScheme/{notificationSchemeId}")
				.resolveTemplate("notificationSchemeId", notificationSchemeId)
			logDebug("target=$target")
			
			Response response = target.request().get()
			if (response.getStatus() == 200) {
				ucdNotificationScheme = response.readEntity(UcdNotificationScheme.class)
			} else {
				String errMsg = UcAdfInvalidValueException.getResponseErrorMessage(response)
				logVerbose(errMsg)
				if (response.getStatus() == 404 || response.getStatus() == 403) {
					if (failIfNotFound) {
						throw new UcAdfInvalidValueException(errMsg)
					}
				} else {
					throw new UcAdfInvalidValueException(errMsg)
				}
			}
		}

		return ucdNotificationScheme
	}
}
