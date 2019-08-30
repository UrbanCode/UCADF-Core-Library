/**
 * This class is used for performing one or more LDAP searches.
 */
package org.urbancode.ucadf.core.integration.ldap

import groovy.util.logging.Slf4j

import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.SizeLimitExceededException
import javax.naming.directory.Attribute
import javax.naming.directory.Attributes
import javax.naming.directory.DirContext
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult

import org.urbancode.ucadf.core.integration.ldap.model.LdapGroupResult
import org.urbancode.ucadf.core.integration.ldap.model.LdapGroupUsersResult
import org.urbancode.ucadf.core.integration.ldap.model.LdapSearchResult
import org.urbancode.ucadf.core.integration.ldap.model.LdapUserGroupsResult
import org.urbancode.ucadf.core.integration.ldap.model.LdapUserResult

@Slf4j
public class LdapManager {
	private final String ldapUrl
	private final String bindDn
	private final String bindPw
	private final String userSearchDc
	private final String groupSearchDc

	// Constructor.	
	public LdapManager() {
	}
	
	public LdapManager(
		final String ldapUrl,
		final String bindDn,
		final String bindPw,
		final String userSearchDc,
		final String groupSearchDc) {
		
		this.ldapUrl = ldapUrl
		this.bindDn = bindDn
		this.bindPw = bindPw
		this.userSearchDc = userSearchDc
		this.groupSearchDc = groupSearchDc
	}

	// Perform a user search.
	public List<LdapUserResult> getUsers(
		final String userId, 
		final int sizeLimit) throws Exception {
		
		return search(
			"LDAP users search for [$userId].",
			userSearchDc, 
			"uid=$userId", 
			LdapUserResult.SEARCH_ATTRNAMES, 
			sizeLimit, 
			LdapUserResult
		)
	}

	// Perform a search to get the groups that a user is a member of.
	public List<LdapUserGroupsResult> getUserGroups(
		final String userId, 
		final int sizeLimit) {
		
		return search(
			"LDAP user groups search for [$userId].",
			groupSearchDc,
			"uniqueMember=uid=$userId*", 
			LdapUserGroupsResult.SEARCH_ATTRNAMES, 
			sizeLimit, 
			LdapUserGroupsResult
		)
	}
	
	// Perform a group search.
	public List<LdapGroupResult> getGroups(
		final String groupName, 
		final int sizeLimit) {
		
		return search(
			"LDAP groups search for [$groupName].",
			groupSearchDc, 
			"cn=$groupName", 
			LdapGroupResult.SEARCH_ATTRNAMES, 
			sizeLimit, 
			LdapGroupResult
		)
	}

	// Perform a search to get the users of a group.
	public List<LdapGroupUsersResult> getGroupUsers(
		final String groupName, 
		final int sizeLimit) {
		
		return search(
			"LDAP group users search for [$groupName].",
			groupSearchDc, 
			"cn=$groupName", 
			LdapGroupUsersResult.SEARCH_ATTRNAMES, 
			sizeLimit, 
			LdapGroupUsersResult
		)
	}

	// Perform a search and return a list of results.
	public List<LdapSearchResult> search(
		final String title, 
		final String searchDc, 
		final String searchString, 
		final String[] searchAttrNames, 
		final int sizeLimit, 
		final Class resultClass) throws Exception {
		
		List<LdapSearchResult> results = new ArrayList<LdapSearchResult>()
		
		// Get the RootDSE search context
		Map<String, String> env = new Hashtable<String, String>()
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
		env.put(Context.PROVIDER_URL, ldapUrl)

		// Use credentials if required.
		if (bindDn) {
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, bindDn);
			env.put(Context.SECURITY_CREDENTIALS, bindPw);
		}
		
		DirContext ctx = new InitialDirContext((Hashtable<String, String>) env)

		Attributes ctxAttrs = ctx.getAttributes(ctx.getNameInNamespace())
		Attribute ctxAttr = ctxAttrs.get("defaultNamingContext")
		
		// Create a SearchControls object that holds information about the scope of a search.
		SearchControls controls = new SearchControls()
		controls.setSearchScope(SearchControls.SUBTREE_SCOPE)

		// Set the size limit for the LDAP search.
		controls.setCountLimit(sizeLimit)

		// Set the LDAP attributes to return.
		controls.setReturningAttributes(searchAttrNames)

		// Perform the LDAP search.
		log.debug("$title ldapUrl [$ldapUrl] searchDc [$searchDc] searchString [$searchString] attributes [${searchAttrNames.iterator().join(',')}]")
		NamingEnumeration<SearchResult> enumer 
		enumer = ctx.search(searchDc, searchString, controls)

		// Create the results list to be returned.
		if (enumer.hasMore()) {
			try {
				while (enumer.hasMore()) {
					SearchResult searchResult = (SearchResult) enumer.next()
					LdapSearchResult result = resultClass.newInstance(searchResult.getAttributes())
					results.add(result)
				}
			} catch (SizeLimitExceededException e) {
				log.info("Returning the maximum results allowed by the specified limit of $sizeLimit.")
			}
		}
		
		return results
	}

	// Displays the list of search results.	
	public displayResults(final List<LdapSearchResult> results) {
		Iterator<LdapSearchResult> i = results.iterator()
		if (i.hasNext()) {
			while (i.hasNext()) {
				LdapSearchResult result = i.next()
				result.displayResult()
			}
		}		
	}
}
