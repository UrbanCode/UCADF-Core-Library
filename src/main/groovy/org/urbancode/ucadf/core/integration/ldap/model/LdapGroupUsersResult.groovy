/**
 * This class represents an LDAP group users result.
 */
package org.urbancode.ucadf.core.integration.ldap.model

import groovy.util.logging.Slf4j

import javax.naming.NamingEnumeration
import javax.naming.directory.Attribute
import javax.naming.directory.Attributes
import javax.naming.ldap.LdapName

@Slf4j
public class LdapGroupUsersResult extends LdapSearchResult {
	// Attributes to return for a group users search.
	public static final String[] SEARCH_ATTRNAMES = [ 
		LDAPATTR_UNIQUEMEMBER
	] as String[]

	public LdapGroupUsersResult(Attributes resultAttributes) throws Exception {
		super(resultAttributes)
	}

	// Get the group members.
	public HashSet<String> getMembers() {
		Set<String> members = new HashSet<String>()
		Attribute member = resultAttributes.get(LDAPATTR_UNIQUEMEMBER)
		if (member != null) {
			for (NamingEnumeration<?> e = member.getAll(); e.hasMore();) {
				Object attrData = e.next()
				LdapName ln = new LdapName((String) attrData)
				if (ln.size() > 0) {
					members.add((String) ln.getRdn(ln.size() - 1).getValue())
				}
			}
		}
		return members
	}
	
	@Override
	public displayResult() {
		println "Group Members=${getMembers()}"
	}
}
