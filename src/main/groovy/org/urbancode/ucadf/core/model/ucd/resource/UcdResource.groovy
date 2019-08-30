/**
 * This class instantiates resource objects.
 */
package org.urbancode.ucadf.core.model.ucd.resource

import java.util.regex.Matcher

import org.urbancode.ucadf.core.model.ucd.agent.UcdAgent
import org.urbancode.ucadf.core.model.ucd.agent.UcdAgentStatusEnum
import org.urbancode.ucadf.core.model.ucd.agentPool.UcdAgentPool
import org.urbancode.ucadf.core.model.ucd.exception.UcdInvalidValueException
import org.urbancode.ucadf.core.model.ucd.general.UcdSecurityTypeObject
import org.urbancode.ucadf.core.model.ucd.property.UcdPropSheet
import org.urbancode.ucadf.core.model.ucd.security.UcdExtendedSecurity
import org.urbancode.ucadf.core.model.ucd.security.UcdSecurityPermissionProperties
import org.urbancode.ucadf.core.model.ucd.tag.UcdTag

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

//@JsonIgnoreProperties(ignoreUnknown = true)
class UcdResource extends UcdSecurityTypeObject {
	/** The path delimiter. */
	public final static String PATHDELIMITER = "/"

	/** The resource ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description
		
	/** The path. */
	String path
	
	/** The property sheet. */	
	UcdPropSheet propSheet

	/** This flag indicates the resource is active. */	
	Boolean active

	/** The type. */
	UcdResourceTypeEnum type
	
	/** The list of tags on the resource. */
	List<UcdTag> tags
	
	/** The parent resource. */
	UcdResource parent
	
	/** The list of child resource trees. */
	List<UcdResourceTree> children
	
	/** The flag that indicates the resource tree has children. */
	Boolean hasChildren
	
	/** The security resource ID. */
	String securityResourceId
	
	/** The security properties. */
	UcdSecurityPermissionProperties security
	
	/** The extended security. */
	UcdExtendedSecurity extendedSecurity

	/** The flag to indicate inherit team. */
	Boolean inheritTeam

	/** The role map. */
	Map role
	
	/** The role properties map. */
	Map roleProperties
	
	/** The list of resource roles. */
	List resourceRoles

	/** The status. */	
	UcdAgentStatusEnum status
	
	/** The flag to indicate discovery failed. */
	Boolean discoveryFailed
	
	/** The flag to indicate prototype. */
	Boolean prototype
	
	/** The impersonation password. */
	String impersonationPassword
	
	/** THe flag to indicate to use sudo for impersonation. */
	Boolean impersonationUseSudo
	
	/** This flag indicates to prevent children or steps from specifying their own impersonation settings. */
	Boolean impersonationForce
	
	/** This flag indicates if the resource has an agent. */
	Boolean hasAgent

	/** The associated agent. */	
	UcdAgent agent
	
	/** The associated agent pool. */
	UcdAgentPool agentPool

	// Constructors.
	UcdResource() {
	}
	
	UcdResource(final String path) {
		this.path = path
	}
	
	UcdResource(
		final String path, 
		final String name, 
		final String description = "") {
		
		this.name = name
		this.path = path
		this.description = description
	}

	UcdResource(
		final UcdResource parent, 
		final String path, 
		final String name, 
		final String description = "") {
		
		this(path, name, description)
		this.parent = parent
	}
	
	/**
	 * Determine if the resource object has a tag with the specified name.
	 * @param tagName The tag name.
	 * @return True if the resource object has the tag.
	 */
	public Boolean hasTag(final String tagName) {
		Boolean foundTag = false
		
		for (UcdTag tag in tags) {
			if (tag.getName().equals(tagName)) {
				foundTag = true
				break
			}
		}
		
		return foundTag
	}
	
	/**
	 * Splits a path into its parent and name parts.
	 * @param path The path.
	 * @return The parent path and name.
	 */
	public static List<String> getParentPathAndName(final String path) {
		Matcher matcher = (path =~ /(.*)\/(.*?)$/)
		if (!matcher.matches()) {
			throw new UcdInvalidValueException("Unable to split path [$path] into parent and name.")
		}
		
		List<String> splitArr = matcher[0]
		String parentPath = splitArr[1]
		String name = splitArr[2]
		
		return [ parentPath, name ]
	}
}
