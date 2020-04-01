/**
 * This class provides page control information for actions that support pages of returned rows.
 */
package org.urbancode.ucadf.core.model.ucadf.loop

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

import org.urbancode.ucadf.core.model.ucadf.UcAdfObject

import groovy.util.logging.Slf4j

@Slf4j
class UcAdfPageLoopControl extends UcAdfObject {
	/** The default loop control property name. */
	public static LOOPCONTROLPROPERTYNAME = "pageLoopControl"
	
	/** The number of rows to return per page. Default is 10. */
	Integer rowsPerPage = 10
	
	/** The current page number. Default is 1. */
	Integer pageNumber = 1
	
	/** The range start returned by the response header. */
	Integer rangeStart
	
	/** The range end returned by the response header. */
	Integer rangeEnd
	
	/** The size returned by the response header. */
	Integer size

	/** The number of pages derived from the size and the rowsPerPage */
	Integer pages
	
	// Process a response to get the content range information.
	public processResponse(Response response) {
		// Get the content range header from the response.
		MultivaluedMap<String, Object> responseHeaders = response.getHeaders()
		String contentRange = responseHeaders.get("Content-Range").get(0)

		// Parse the content range that's returned as string format: RangeStart-RangeEnd/Size.
		String rangeStartStr, rangeEndStr, sizeStr
		(rangeStartStr, rangeEndStr, sizeStr) = contentRange.split(/[-\/]/)

		// Convert the parsed strings to integers.
		// The items prefix was added in 7.0.4.
		rangeStart = Integer.valueOf(rangeStartStr.replaceAll(/items /, ""))
		rangeEnd = Integer.valueOf(rangeEndStr)
		size = Integer.valueOf(sizeStr)
		
		pages = (size + rowsPerPage - 1) / rowsPerPage
		
		log.debug("rangeStart=[$rangeStart] rangeEnd=[$rangeEnd] size=[$size] rowsPerPage=[$rowsPerPage] pages=[$pages] pageNumber=[$pageNumber]")
	}	
}
