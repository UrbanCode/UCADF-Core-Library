/**
 * This action gets the component process request task completion properties and optionally sets properties in a specified process.
 */
package org.urbancode.ucadf.core.action.ucd.componentProcessRequest

import java.text.SimpleDateFormat
import java.util.regex.Matcher

import org.urbancode.ucadf.core.action.ucd.user.UcdGetUser
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequest
import org.urbancode.ucadf.core.model.ucd.componentProcessRequest.UcdComponentProcessRequestTask
import org.urbancode.ucadf.core.model.ucadf.exception.UcAdfInvalidValueException
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
	
	/** The flag that indicates fail if the task is not found. Default is true. */
	String failIfNotFound = true
	
	/**
	 * Runs the action.	
	 * @return The task information.
	 */
	@Override
	public TaskReturn run() {
		// Validate the action properties.
		validatePropsExist()

		TaskReturn taskReturn = new TaskReturn()
		
		String completedBy, completedByDisplayName
		Long completedOn
		String completedOnTs
		
		// Get the component process request information.
		UcdComponentProcessRequest ucdComponentProcessRequest = actionsRunner.runAction([
			action: UcdGetComponentProcessRequest.getSimpleName(),
			actionInfo: false,
			actionVerbose: false,
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
            logVerbose("Completed by [$completedBy] on [$completedOn].")
			UcdUser completedByUser = getUserInfoByDisplayName(completedBy)
			if (!completedByUser) {
				throw new UcAdfInvalidValueException("Unable to determine user name from user display name [$completedBy]")
			}
			completedBy = completedByUser.getName()
			completedByDisplayName = completedByUser.getDisplayName()
			completedOnTs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(completedOn)
			outProps.put(PROP_TASKCOMPLETEDBY, completedBy)
			outProps.put(PROP_TASKCOMPLETEDBYDISPLAYNAME, completedByDisplayName)
			outProps.put(PROP_TASKCOMPLETEDON, completedOnTs)

			// Set the return values.
			taskReturn.setTaskCompletedBy(completedBy)
			taskReturn.setTaskCompletedByDisplayName(completedByDisplayName)
			taskReturn.setTaskCompletedOn(completedOnTs)
		} else {
			throw new UcAdfInvalidValueException("Unable to get task information.")
		}

		return taskReturn
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
			actionInfo: false,
			actionVerbose: false,
			user: user,
			failIfNotFound: false
		])

		return ucdUser
	}

	/** This class returns task information. */
	static public class TaskReturn {
		String taskCompletedBy
		String taskCompletedByDisplayName
		String taskCompletedOn
	}
}
