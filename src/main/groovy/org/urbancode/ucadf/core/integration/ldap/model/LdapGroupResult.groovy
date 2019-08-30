/**
 * This class represents an LDAP group result.
 */
package org.urbancode.ucadf.core.integration.ldap.model

import javax.naming.NamingEnumeration
import javax.naming.directory.Attribute
import javax.naming.directory.Attributes
import javax.naming.ldap.LdapName

public class LdapGroupResult extends LdapSearchResult {
	// Attributes to return for a group search.
	public static final String[] SEARCH_ATTRNAMES = [ 
		LDAPATTR_CN
	] as String[]

	public LdapGroupResult(final Attributes resultAttributes) throws Exception {
		super(resultAttributes)
	}

	public String getName() {
		return getAttributeValue(LDAPATTR_CN)
	}
	
	// Get the group members.
	public HashSet<String> getMembers() {
		Set<String> members = new HashSet<String>()
		Attribute member = resultAttributes.get(LDAPATTR_UNIQUEMEMBER)
		if (member != null) {
			for (NamingEnumeration<?> e = member.getAll(); e.hasMore();) {
				Object attrData = e.next()
				LdapName ln = new LdapName((String) attrData)
				members.add((String) ln.getRdn(ln.size() - 1).getValue())
			}
		}
		return members
	}
	
	@Override
	public displayResult() {
		println "Group Name [${getName()}]."
		HashSet<String> members = getMembers()
		if (members.size() > 0) {
			println "Members [$members]."
		}
	}
}
