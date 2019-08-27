/**
 * This class represents an LDAP user groups result.
 */
package org.urbancode.ucadf.core.integration.ldap.model

import groovy.util.logging.Slf4j

import javax.naming.directory.Attributes

@Slf4j
public class LdapUserGroupsResult extends LdapSearchResult {
	// Attributes to return for a group search.
	public static final String[] SEARCH_ATTRNAMES = [ 
		LDAPATTR_CN
	] as String[]

	public LdapUserGroupsResult(final Attributes resultAttributes) throws Exception {
		super(resultAttributes)
	}

	public String getGroupName() {
		return getAttributeValue(LDAPATTR_CN)
	}	
	
	@Override
	public displayResult() {
		println "Group Name=${getGroupName()}"
	}
}
