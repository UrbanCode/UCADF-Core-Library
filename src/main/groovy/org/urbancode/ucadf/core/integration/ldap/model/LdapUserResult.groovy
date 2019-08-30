/**
 * This class represents an LDAP user result.
 */
package org.urbancode.ucadf.core.integration.ldap.model

import groovy.util.logging.Slf4j

import javax.naming.directory.Attributes

@Slf4j
public class LdapUserResult extends LdapSearchResult {
	// Attributes to return for a user search.
	public static final String[] SEARCH_ATTRNAMES = [ 
		LDAPATTR_UID, 
		LDAPATTR_CN,
		LDAPATTR_MAIL
	] as String[]

	public LdapUserResult(final Attributes resultAttributes) throws Exception {
		super(resultAttributes)
	}
	
	public String getName() {
		return getAttributeValue(LDAPATTR_UID)
	}
	
	public String getDisplayName() {
		return getAttributeValue(LDAPATTR_CN)
	}
	
	public String getMail() {
		return getAttributeValue(LDAPATTR_MAIL)
	}
	
	@Override
	public displayResult() {
		println "User ID [${getName()}]"
		println "Display Name [${getDisplayName()}]"
		println "Mail [${getMail()}]"
	}
}
