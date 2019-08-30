/**
 * This action gets user authentication token objects. Since the actual encrypted token values can't be returned the primary purpose for this is to find tokens to delete.
 */
package org.urbancode.ucadf.core.action.ucd.authToken

import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucd.authToken.UcdAuthToken
import org.urbancode.ucadf.core.model.ucd.authToken.UcdAuthTokenTable
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterField
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldClassEnum
import org.urbancode.ucadf.core.model.ucd.filterField.UcdFilterFieldTypeEnum

class UcdGetAuthTokens extends UcAdfAction {
	/** (Optional) The specific user name. */	
	String user = ""
	
	/** (Optional) The filter fields. */
	List<UcdFilterField> filterFields = []

	/**
	 * Runs the action.	
	 * @return The list of authentication tokens.
	 */
	@Override
	public List<UcdAuthToken> run() {
		// Validate the action properties.
		validatePropsExist()

		List<UcdAuthToken> ucdAuthTokens = []

		// If a user was specified then add it to the filter list.
		if (user) {
			filterFields.add(
				new UcdFilterField(
					"user.name",
					user,
					UcdFilterFieldTypeEnum.eq,
					UcdFilterFieldClassEnum.String
				)
			)
		}
		
		// Couldn't find a better API than using the table query.
		WebTarget target = UcdFilterField.addFilterFieldQueryParams(
			ucdSession.getUcdWebTarget().path("/security/authtoken/table"),
			filterFields
		)

		logDebug("target=$target")
		
		Response response = target.request().get()
		if (response.getStatus() == 200) {
			UcdAuthTokenTable ucdAuthTokenTable = response.readEntity(UcdAuthTokenTable.class)
			ucdAuthTokens = ucdAuthTokenTable.getRecords()
		} else {
            throw new UcdInvalidValueException(response)
		}
				
		return ucdAuthTokens
	}
}
