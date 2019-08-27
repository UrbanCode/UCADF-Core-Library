/**
 * This enumeration represents the import type values.
 */
package org.urbancode.ucadf.core.model.ucd.importExport

enum UcdImportTypeEnum {
	/** Use the existing entity if it exists. */
	USE_EXISTING_IF_EXISTS,
	
	/** Creat a entity even if it already exists. */
	CREATE_NEW_IF_EXISTS,
	
	/** Fail if the entity already exists. */
	FAIL_IF_EXISTS,
	
	/** Fail if the entity doesn't already exist. */
	FAIL_IF_DOESNT_EXIST,
	
	/** Upgrade the existing entity. */
	UPGRADE_IF_EXISTS
}
