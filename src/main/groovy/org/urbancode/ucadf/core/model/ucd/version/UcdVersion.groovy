/**
 * This class instantiates a version object.
 */
package org.urbancode.ucadf.core.model.ucd.version

import org.urbancode.ucadf.core.model.ucd.component.UcdComponent
import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.property.UcdProperty
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties
import org.urbancode.ucadf.core.model.ucd.status.UcdStatus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdVersion extends UcdObject {
	/** The version ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The version type. */	
	UcdVersionTypeEnum type
	
	/** The created date. */
	Long created
	
	/** The creator. */
	String creator
	
	/** The flag that indicates the version is active. */
	Boolean active
	
	/** The flag that indicates the version is deleted. */
	Boolean deleted
	
	/** The flag that indicates the version is archived. */
	Boolean archived
	
	/** The flag that indicates the integration failed. */
	Boolean integrationFailed
	
	/** The size of the version on disk. */
	Long sizeOnDisk
	
	/** The associated component. */
	UcdComponent component
	
	/** The list of property sheets. */
	List<UcdPropSheet> propSheets
	
	/** The total size. */
	Long totalSize
	
	/** The total count. */
	Long totalCount
	
	/** The latest status. */
	UcdStatus latestStatus
	
	/** The list of statuses. */
	List<UcdStatus> statuses
	
	/** The list of properties. */
	List<UcdProperty> properties
	
	/** The security resource ID. */
	String securityResourceId
	
	/** The security properties. */
	UcdSecurityPermissionProperties security

	/** Flag that indicates version is importing. */
	Boolean importing

	/** Z/OS properties. */
	Long totalZosAddSequentialCount
	Long totalZosDelSequentialcount
	Long totalZosAddPdsCount
	Long totalZosDelPdsCount
	Long totalZosAddPdsMemberCount
	Long totalZosDelPdsMemberCount
	Long totalZosGenericCount
	Long totalZosGenericMemberCount
	Long totalZosAddUssDirectoryCount
	Long totalZosDelUssDirectoryCount
	Long totalZosAddUssFileCount
	Long totalZosDelUssFileCount
	
	// Constructors.	
	UcdVersion() {
	}
}
