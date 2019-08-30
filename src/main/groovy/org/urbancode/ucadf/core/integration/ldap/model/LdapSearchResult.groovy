/**
 * This class contains a single LDAP search result
 */
package org.urbancode.ucadf.core.integration.ldap.model

import groovy.util.logging.Slf4j

import javax.naming.NamingEnumeration
import javax.naming.directory.Attribute
import javax.naming.directory.Attributes

@Slf4j
public abstract class LdapSearchResult {
	public static final String LDAPATTR_UID = "uid"
	public static final String LDAPATTR_MAIL = "mail"
	public static final String LDAPATTR_CN = "cn"
	public static final String LDAPATTR_UNIQUEMEMBER = "uniqueMember"

	// The attributes returned from a search.
	protected Attributes resultAttributes

	// Constructor.	
	public LdapSearchResult(Attributes resultAttributes) {
		this.resultAttributes = resultAttributes
		//println resultAttributes
	}

	// Display a search result.
	public displayResult() {
		displayAttributes()
	}
	
	// Display the LDAP attributes from the search results.
	public displayAttributes() {
		NamingEnumeration ae = resultAttributes.getAll()
		while (ae.hasMore()) {
			Attribute attr = ae.next();
			NamingEnumeration e = attr.getAll()
			while (e.hasMore()) {
				 println "attribute=[${attr.getID()}] value=[${e.next()}]"
			}
		}
	}

	// Get an attribute value from the attributes collection.	
	public String getAttributeValue(String attributeName) {
		String value
		Attribute attr = resultAttributes.get(attributeName)
		if (attr == null) {
			value = ""
		} else {
			value = attr.toString().replaceAll(attributeName + ":\\s*", "")
		}

		return value		
	}
}
