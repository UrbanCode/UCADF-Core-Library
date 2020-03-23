/**
 * This action sends email.
 */
package org.urbancode.ucadf.core.action.ucd.system

import javax.mail.Multipart
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.ws.rs.core.MediaType

import org.urbancode.ucadf.core.action.ucd.team.UcdGetTeamUsers
import org.urbancode.ucadf.core.action.ucd.user.UcdGetUser
import org.urbancode.ucadf.core.actionsrunner.UcAdfAction
import org.urbancode.ucadf.core.model.ucadf.UcAdfSecureString
import org.urbancode.ucadf.core.model.ucd.system.UcdSystemConfiguration
import org.urbancode.ucadf.core.model.ucd.team.UcdTeamRole
import org.urbancode.ucadf.core.model.ucd.user.UcdUser

import groovy.text.Template
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import groovy.util.logging.Slf4j

@Slf4j
class UcdSendMail extends UcAdfAction {
	// Action properties.
	/** The subject. */
	String subject
	
	/** (Optional) The message. */
	String message = ""
	
	/** The media type. Default is TEXT_PLAIN. */
	String mediaType = MediaType.TEXT_PLAIN
	
	/** (Optional) The list of files to attach. */
	List<File> attachFiles = []
	
	/** (Optional) The template text compatible with the MarkupTemplateEngine. */
	String templateText = ""
	
	/** (Optional) The template properties compatibile with the MarkupTemplateEngine. */
	Map<String, String> templateProperties = [:]
	
	/** (Optional) The list of full email addresses. */
	List<String> addresses = []
	
	/** (Optional) The list of user names or IDs. */
    List<String> users = []
	
	/** (Optional) The list of team roles. */
	List<UcdTeamRole> teamRoles = []
	
	/** (Optional) The list of user names to exclude. */
	List<String> excludeUsers = []
	
	/** (Optional) The SMTP email user ID. */
	UcAdfSecureString emailUserId = new UcAdfSecureString()
	
	/** (Optional) The SMTP email password. */
	UcAdfSecureString emailUserPw = new UcAdfSecureString()

	// Private properties.
	private emailUserIdStr
	private emailUserPwStr
	private derivedMessage
	private Set<String> derivedAddresses = new HashSet<String>()
	private List<String> excludeUsersLc = []
	
	/**
	 * Runs the action.	
	 */
	@Override
	public Object run() {
		// Validate the action properties.
		validatePropsExist()
		
		emailUserIdStr = emailUserId.toString()
		emailUserPwStr = emailUserPw.toString()

		// Lower case the excluded users list.
		excludeUsersLc = excludeUsers*.toLowerCase()
		
		// If using a template then process it to derive the message text.
		derivedMessage = message
		if (templateText) {
			TemplateConfiguration config = new TemplateConfiguration()
			config.setAutoNewLine(true)
			config.setAutoIndent(true)
			MarkupTemplateEngine engine = new MarkupTemplateEngine(config)
			Template template = engine.createTemplate(templateText)
	
			// Render output for template using provided properties for the template.
			Writer writer = new StringWriter()
			Writable output = template.make(templateProperties)
			output.writeTo(writer)
			derivedMessage = writer.toString()
		}

		// Add the address to the derived list to which mail will be sent.		
		// Each address in the list may be a comma-delimited set of addresses that needs to be split into mulitple addresses.
		addresses.each { addAddress(it) }

		// Add the addresses for the specified users.
		addUserAddresses()
		
		// Add the addresses for the specified team roles.
		addTeamRoleAddresses()

		// Send email to the addresses.		
        if (derivedAddresses.size() > 0) {
			sendMailToAddresses()
		} else {
			log.info("No email addresses found.")
		}
	}
	
	// Add the addresses for the specified users.
	private addUserAddresses() {
		for (user in users) {
			UcdUser ucdUser = actionsRunner.runAction([
				action: UcdGetUser.getSimpleName(),
				actionInfo: actionInfo,
				user: user,
				failIfNotFound: false
			])

			if (!ucdUser) {
				logVerbose("Unable to find information about user [$user].")
			} else {
				addUserAddress(ucdUser)
			}
		}
	}
	
    // Add the addresses for the specified team roles.
    private addTeamRoleAddresses() {
		for (teamRole in teamRoles) {
			List<UcdUser> ucdUsers = actionsRunner.runAction([
				action: UcdGetTeamUsers.getSimpleName(),
				actionInfo: actionInfo,
				team: teamRole.getTeam(),
				role: teamRole.getRole()
			])

            for (ucdUser in ucdUsers) {
				addUserAddress(ucdUser)
            }
		}
    }

	// Add a user's address to the set of addresses.
	private addUserAddress(final UcdUser ucdUser) {
		if (!excludeUsersLc.contains(ucdUser.getName().toLowerCase())) {
			if (ucdUser.getEmail()) {
				addAddress(ucdUser.getEmail())
			} else {
				logVerbose("Unable to find information about user [${ucdUser.getName()}].")
			}
		}
	}
	
	// Add an address to the set of addresses.
	private addAddress(final String address) {
		// Split the to addresses in case there's more than one.
		StringTokenizer tok = new StringTokenizer(address, ",")
		while(tok.hasMoreElements()){
			derivedAddresses.add(new InternetAddress(tok.nextElement().toString()))
		}
	}
	
	// Send mail to addresses.		
	private sendMailToAddresses() {
		// Get the UrbanCode system configuration.
		UcdSystemConfiguration ucdSystemConfiguration = actionsRunner.runAction([
			action: UcdGetSystemConfiguration.getSimpleName(),
			actionInfo: actionInfo
		])

		String mailHost = ucdSystemConfiguration.getDeployMailHost()
		String mailPort = ucdSystemConfiguration.getDeployMailPort()
		String mailSender = ucdSystemConfiguration.getDeployMailSender()
		String mailSecure = ucdSystemConfiguration.getDeployMailSecure()
		if (!emailUserIdStr) {
			emailUserIdStr = ucdSystemConfiguration.getDeployMailUsername()
		}
		
		log.info("Sending mail from [$mailSender] [$emailUserIdStr] to [$mailHost:$mailPort] [$derivedAddresses] subject [$subject].")
		
		if (mailSecure && !emailUserPwStr) {
			log.info(("Skipping sending email because configuration is for secure email but no password was provided."))
		} else {
			// TODO: Needs to be more generic.
    		// Set up the properties for the mail session
    		Properties mprops = new Properties()
    		mprops.setProperty("mail.transport.protocol", "smtp")
    		mprops.setProperty("mail.host", mailHost)
    		mprops.setProperty("mail.smtp.port", mailPort)
			mprops.setProperty("mail.smtp.auth", mailSecure)
    		mprops.setProperty("mail.smtp.starttls.enable", "true")
			mprops.setProperty("mail.smtp.ssl.enable", "false")
			mprops.setProperty("mail.smtp.ssl.protocols", "TLSv1.2")
			mprops.setProperty("mail.smtp.debug", "true")
			
			mprops.each { k, v ->
				println "$k=$v"
			}
			
    		Session lSession = Session.getInstance(mprops,
    			new javax.mail.Authenticator() {
    				protected PasswordAuthentication getPasswordAuthentication() {
    				return new PasswordAuthentication(emailUserIdStr, emailUserPwStr)
    			}
    		})
    
    		// Create the message		
    		MimeMessage mimeMessage = new MimeMessage(lSession)
    		
    		InternetAddress[] to = new InternetAddress[derivedAddresses.size()]
    		to = (InternetAddress[]) derivedAddresses.toArray(to)
    		mimeMessage.setRecipients(MimeMessage.RecipientType.TO,to)
    		mimeMessage.setFrom(new InternetAddress(mailSender))
    		mimeMessage.setSubject(subject)
			
			if (attachFiles.size() > 0) {
				// Message with file attachments.
				MimeBodyPart msgBodyPart = new MimeBodyPart()
				msgBodyPart.setContent(derivedMessage, mediaType)
		 
				Multipart multipart = new MimeMultipart()
				multipart.addBodyPart(msgBodyPart)
		 
				for (String filePath : attachFiles) {
					MimeBodyPart attachPart = new MimeBodyPart()
					attachPart.attachFile(filePath)
					multipart.addBodyPart(attachPart)
				}
		 
				mimeMessage.setContent(multipart)
			} else {
				// Just a simple message body.
    			mimeMessage.setContent(derivedMessage, mediaType)
			}
			
    		// Send the message
    		Transport transporter = lSession.getTransport("smtp")
    		transporter.connect()
    		transporter.send(mimeMessage)
			
			log.info("Mail sent.")
        }
	}
}
