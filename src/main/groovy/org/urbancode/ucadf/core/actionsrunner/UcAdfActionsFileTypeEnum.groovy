package org.urbancode.ucadf.core.actionsrunner

/**
 * Enumeration of the names of action file types.
 */
enum UcAdfActionsFileTypeEnum {
	AUTO("AUTO"),
	JSON("JSON"),
	YAML("YAML"),
	PROPERTIES("PROPERTIES")
	
	private String fileType

	// Constructor.		
	UcAdfActionsFileTypeEnum(final String fileType) {
		this.fileType = fileType
	}
	
	public String getFileType() {
		return fileType
	}
}
