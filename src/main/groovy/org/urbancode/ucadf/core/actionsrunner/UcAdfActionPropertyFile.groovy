package org.urbancode.ucadf.core.actionsrunner

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcAdfActionPropertyFile extends UcdObject {
	String fileName
	UcAdfActionsFileTypeEnum fileType = UcAdfActionsFileTypeEnum.AUTO
	Boolean failNotFound = true

	// Contructors.	
	UcAdfActionPropertyFile() {
	}
	
	UcAdfActionPropertyFile(
		final String fileName,
		final UcAdfActionsFileTypeEnum fileType = UcAdfActionsFileTypeEnum.AUTO,
		final Boolean failNotFound = true) {

		this.fileName = fileName
		this.fileType = fileType
		this.failNotFound = failNotFound		
	}
}
