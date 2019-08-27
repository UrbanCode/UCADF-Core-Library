/**
 * This action gets the component process request task completion properties and optionally sets properties in a specified process.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcessRequest

import java.text.SimpleDateFormat
import java.util.regex.Matcher

import org.urbancode.ucadf.core.action.ucd.applicationProcessRequest.UcdGetApplicationProcessRequest
import org.urbancode.ucadf.core.action.ucd.applicationProcessRequest.UcdSetApplicationProcessRequestProperties
import org.urbancode.ucadf.core.action.ucd.user.UcdGetUser
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.applicationProcessRequest.UcdApplicationProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequestTask
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

class UcdGetComponentProcessRequestTaskCompletionProperties extends UcAdfAction {
	// Constants.
	private final static String PROP_TASKCOMPLETEDBY = "ucAdfTaskCompletedBy"	
	private final static String PROP_TASKCOMPLETEDBYDISPLAYNAME = "ucAdfTaskCompletedByDisplayName"	
	private final static String PROP_TASKCOMPLETEDON = "ucAdfTaskCompletedOn"

	// Action properties.
	/** The component process request ID. */
	String requestId
	
	/** The task name. */
	String taskName
	
	/** (Optional) The request ID of a process for which the componention properties should be set. */
	String setPropsProcessRequestId = ""
	
	/** (Optional) The propery name suffix for the completion properties set on the specified request. */
	String setPropsProcessRequestSuffix = ""
	
	/** The flag that indicates fail if the task is not found. Default is true. */
	String failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The map of component process request properties having the property name as the key and the value a property object.
	 */
	@Override
	public Map<String, UcdProperty> run() {
		// Validate the action properties.
		validatePropsExist()

		Map<String, UcdProperty> propertiesMap = [:]

		String completedBy, completedByDisplayName
		Long completedOn
		String completedOnTs
		
		// Get the component process request information.
		UcdComponentProcessRequest ucdComponentProcessRequest = actionsRunner.runAction([
			action: UcdGetComponentProcessRequest.getSimpleName(),
			actionInfo: false,
			requestId: requestId,
			failIfNotFound: failIfNotFound
		])

		// Find the manual task in the component process request
		UcdComponentProcessRequestTask task = ucdComponentProcessRequest.findChildTask(taskName)
		
		if (task) {
			// Get the task custom property values.
			outProps = task.getTaskCustomPropertyValues()

			// Get the task completed information
			completedBy = task.getCompletedBy()
			completedOn = task.getCompletedOn()
            logInfo("Completed by [$completedBy] on [$completedOn].")
			UcdUser completedByUser = getUserInfoByDisplayName(completedBy)
			if (!completedByUser) {
				throw new UcdInvalidValueException("Unable to determine user name from user display name [$completedBy]")
			}
			completedBy = completedByUser.getName()
			completedByDisplayName = completedByUser.getDisplayName()
			completedOnTs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(completedOn)
			outProps.put(PROP_TASKCOMPLETEDBY, completedBy)
			outProps.put(PROP_TASKCOMPLETEDBYDISPLAYNAME, completedByDisplayName)
			outProps.put(PROP_TASKCOMPLETEDON, completedOnTs)

			// Construct the properties list.
			List<UcdProperty> ucdProperties = [
				[
					name: "${PROP_TASKCOMPLETEDBY}${setPropsProcessRequestSuffix}",
					value: completedBy
				],
				[
					name: "${PROP_TASKCOMPLETEDBYDISPLAYNAME}${setPropsProcessRequestSuffix}",
					value: completedByDisplayName
				],
				[
					name: "${PROP_TASKCOMPLETEDON}${setPropsProcessRequestSuffix}",
					value: completedOnTs
				]
			]
			
			// Set process request property values.
			if (setPropsProcessRequestId) {
				// Determine if the request ID is for an applicatiion or component process.
				UcdApplicationProcessRequest appProcessRequest = actionsRunner.runAction([
					action: UcdGetApplicationProcessRequest.getSimpleName(),
					requestId: setPropsProcessRequestId
				])
		
				// Set properties on either on application or component process request.
				if (appProcessRequest) {
					actionsRunner.runAction([
						action: UcdSetApplicationProcessRequestProperties.getSimpleName(),
						requestId: setPropsProcessRequestId,
						properties: ucdProperties
					])
				} else {
					actionsRunner.runAction([
						action: UcdSetComponentProcessRequestProperties.getSimpleName(),
						requestId: setPropsProcessRequestId,
						properties: ucdProperties
					])
				}
			}
		} else {
			throw new UcdInvalidValueException("Unable to get task information.")
		}

		// Construct the return properties map.
		outProps.each { name, value ->
			propertiesMap.put(name, new UcdProperty(name, value))
		}

		return propertiesMap
	}	

	// Get information about a user using the display name to perform the search.
	public UcdUser getUserInfoByDisplayName(final String userDisplayName) {
		String user = userDisplayName
		String regEx = /\((.*?)\)/
		Matcher userNameMatch = (userDisplayName =~ regEx)
		if (userNameMatch.size() == 1) {
			user = (userDisplayName =~ regEx)[0][1]
		}

		// Validate the user exists.
		UcdUser ucdUser = actionsRunner.runAction([
			action: UcdGetUser.getSimpleName(),
			user: user,
			failIfNotFound: false
		])

		return ucdUser
	}
}
