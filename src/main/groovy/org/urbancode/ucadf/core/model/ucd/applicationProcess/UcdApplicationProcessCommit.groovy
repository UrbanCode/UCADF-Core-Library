/**
 * This class instantiates application process commit objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationProcess

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationProcessCommit extends UcdObject {
	Long version
	Long commitId
	String committer
	Long commitTime
	String comment
}
