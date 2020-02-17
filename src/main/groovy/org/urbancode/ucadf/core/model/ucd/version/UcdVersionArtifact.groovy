/**
 * This class instantiates a version artifact object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
abstract class UcdVersionArtifact extends UcdObject {
	/** The artifact name. */
	String name
	
	/** The artifact . */
	String path
	
	/** The artifact . */
	Long version
	
	/** The artifact . */
	@JsonProperty('$ref')
	String ref
	
	/** The artifact type. */
	UcdVersionArtifactTypeEnum type
	
	/** The flag that indicates if the item has children. */
	Boolean children
	
	/** The total size. */
	Long totalSize
	
	/** The total count. */
	Long totalCount
	
	/** The last modified timestamp. */
	Long lastModified
	
	/** The length. */
	Long length
	
	/** The deployment type. */
	String deployType
	
	/** The flag that indicates metadata only. */
	Boolean metaOnly
	
	// Constructors.	
	UcdVersionArtifact() {
	}
	
	abstract public List<UcdVersionArtifact> getChildArtifacts()
	
	abstract public setChildArtifacts(List<UcdVersionArtifact> artifacts)
}
