/**
 * This class instantiates team objects.
 */
package org.urbancode.ucadf.core.model.ucd.team

import org.urbancode.ucadf.core.model.ucd.general.UcdObject
import org.urbancode.ucadf.core.model.ucd.role.UcdRoleMapping

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UcdTeam extends UcdObject {
	/** The team ID. */
	String id
	
	/** The name. */
	String name
	
	/** The description. */
	String description

	/** The flag that indicates the team is deletable. */	
	Boolean isDeletable
	
	/** The list of role mappings. */
	List<UcdRoleMapping> roleMappings

	// Constructors.	
	UcdTeam() {
	}

	/**
	 * Determine if a user is a member of the team.	
	 * @param role The role name or ID.
	 * @param user The user name or ID.
	 * @return True if user is a team member.
	 */
	@JsonIgnore
	public Boolean isUserTeamMember(
		final String role, 
		final String user) {
		
		Boolean isMember = false
		
		// See if the user is a member of the team.
		for (entity in roleMappings) {
			if (entity.getRole() && (entity.getRole().getName() == role || entity.getRole().getId() == role)) {
				if (user && entity.getUser() && (entity.getUser().getName().equals(user) || entity.getUser().getId().equals(user))) {
					isMember = true
					break
				}
			}
		}
		
		return isMember
	}
	
	/**
	 *  See if the specified group is a member of the specified team info role.
	 * @param role The role name or ID.
	 * @param group The group or ID.
	 * @return True if team group is a member of the team.
	 */
	@JsonIgnore
	public Boolean isGroupTeamMember(
		final String role, 
		final String group) {
		
		Boolean isMember
		
		for (entity in roleMappings) {
			if (entity.getRole() && (entity.getRole().getName() == role || entity.getRole().getId() == role)) {
				if (group && entity.getGroup() && (entity.getGroup().getName().equals(group) || entity.getGroup().getName().equals(group))) {
					isMember = true
					break
				}
			}
		}
		
		return isMember
	}
}
