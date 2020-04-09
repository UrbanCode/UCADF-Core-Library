/**
 * This class instantiates application template commit objects.
 */
package org.urbancode.ucadf.core.model.ucd.applicationTemplate

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdApplicationTemplateCommit extends UcdObject {
	/** The commit ID. */
	Long commitId
	
	/** The committer. */
	String committer
	
	/** The commit date. */
	Long commitTime
	
	/** The commit comment. */
	String comment
	
	// Constructors.	
	UcdApplicationTemplateCommit() {
	}
}
