package org.urbancode.ucadf.core.actionsrunner

import org.urbancode.ucadf.core.model.ucd.general.UcdObject

class UcAdfActionPropertyFile extends UcdObject {
	String fileName
	String fileType = UcAdfActions.FILETYPE_AUTO
	Boolean failNotFound = true
	
	UcAdfActionPropertyFile() {
	}
}
