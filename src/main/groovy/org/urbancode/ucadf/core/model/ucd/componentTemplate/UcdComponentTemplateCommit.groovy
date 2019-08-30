/**
 * This class instantiates component template commit objects.
 */
package org.urbancode.ucadf.core.model.ucd.componentTemplate

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdComponentTemplateCommit extends UcdObject {
	/** The commit ID. */
	Long commitId
	
	/** The committer. */
	String committer
	
	/** The commit date. */
	Long commitTime
	
	/** The commit comment. */
	String comment
	
	// Constructors.	
	UcdComponentTemplateCommit() {
	}
}
