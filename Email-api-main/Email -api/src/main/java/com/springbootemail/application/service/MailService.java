package com.springbootemail.application.service;

import ch.qos.logback.core.net.server.Client;
import com.springbootemail.application.Repository.MailsRepository;
import com.springbootemail.application.Repository.UsersRepository;
import com.springbootemail.application.model.Mails;
import com.springbootemail.application.model.Retrive;
import com.springbootemail.application.model.User;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import org.apache.tomcat.jni.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.tools.JavaFileManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class MailService {

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendEmail(User user) {

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(user.getTo());
		mail.setSubject(user.getSubject());
		mail.setFrom(user.getFrom());
		mail.setText(user.getBody());
		javaMailSender.send(mail);
	}

	public void sendEmailWithAttachment(User user) throws MessagingException {

		MimeMessage message = javaMailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		helper.setTo(user.getTo());
		helper.setSubject(user.getSubject());
		helper.setText(user.getBody());
		helper.setFrom(user.getFrom());
		ClassPathResource classPathResource = new ClassPathResource("Attachment.pdf");
		helper.addAttachment(Objects.requireNonNull(classPathResource.getFilename()), classPathResource);

		javaMailSender.send(message);
	}

	public void downloadEmailAttachments(Retrive retrive) {

		Properties properties = new Properties();
		String port = "993";
		properties.put("mail.pop3.host", "pop.gmail.com");
		properties.put("mail.pop3.port", port);
		properties.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		properties.setProperty("mail.pop3.socketFactory.fallback", "false");
		properties.setProperty("mail.pop3.socketFactory.port",
				String.valueOf(port));
		Session session = Session.getDefaultInstance(properties);

		try {
			// connects to the message store
			Store store = session.getStore("pop3");
			store.connect(retrive.getUserName(), retrive.getPassword());

			// opens the inbox folder
			Folder folderInbox = store.getFolder("INBOX");
			folderInbox.open(Folder.READ_ONLY);

			// fetches new messages from server
			Message[] arrayMessages = folderInbox.getMessages();

			for (int i = 0; i < arrayMessages.length; i++) {
				Message message = arrayMessages[i];
				Address[] fromAddress = message.getFrom();
				String from = fromAddress[0].toString();
				String subject = message.getSubject();
				String sentDate = message.getSentDate().toString();

				String contentType = message.getContentType();
				String messageContent = "";

				// store attachment file name, separated by comma
				String attachFiles = "";

				if (contentType.contains("multipart")) {
					// content may contain attachments
					Multipart multiPart = (Multipart) message.getContent();
					int numberOfParts = multiPart.getCount();
					for (int partCount = 0; partCount < numberOfParts; partCount++) {
						MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
						if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
							// this part is attachment
							String fileName = part.getFileName();
							attachFiles += fileName + ", ";
							part.saveFile(retrive.getSaveDirectory() + File.separator + fileName);
						} else {
							// this part may be the message content
							messageContent = part.getContent().toString();
						}
					}

					if (attachFiles.length() > 1) {
						attachFiles = attachFiles.substring(0, attachFiles.length() - 2);
					}
				} else if (contentType.contains("text/plain")
						|| contentType.contains("text/html")) {
					Object content = message.getContent();
					if (content != null) {
						messageContent = content.toString();
					}
				}

				// print out details of each message
				System.out.println("Message #" + (i + 1) + ":");
				System.out.println("\t From: " + from);
				System.out.println("\t Subject: " + subject);
				System.out.println("\t Sent Date: " + sentDate);
				System.out.println("\t Message: " + messageContent);
				System.out.println("\t Attachments: " + attachFiles);
			}

			// disconnect
			folderInbox.close(false);
			store.close();
		} catch (NoSuchProviderException ex) {
			System.out.println("No provider for pop3.");
			ex.printStackTrace();
		} catch (MessagingException ex) {
			System.out.println("Could not connect to the message store");
			ex.printStackTrace();
		} catch (FileNotFoundException ex){
			ex.printStackTrace();
			System.out.println("could not determine file location");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}
	private String[] Email_list={"truthvikreddy251@gmail.com"};//Enter whitelisted emails
	Properties properties = null;
	private Session session = null;
	private Store store = null;
	private IMAPFolder inbox = null;

	private String userName = "";// provide user name
	private String password = "";// provide password
	private LocalDateTime recieved;
private Boolean notfound=true;


	public Boolean getNotfound() {
		return notfound;
	}

	String saveDirectory = null;

	@Autowired
private MailsRepository mailsRepository;
private Mails mails;
	public void readMails(Retrive retrive) throws IOException {

switch (retrive.getDomain()) {
	case "gmail":
		System.out.println("domain name: " + retrive.getDomain());
		properties = new Properties();
		properties.setProperty("mail.host", "imap.gmail.com");
		properties.setProperty("mail.port", "993");
		properties.setProperty("mail.transport.protocol", "imaps");
		properties.setProperty("mail.imap.partialfetch", "false");
		properties.setProperty("mail.imap.starttls.enable", "true");
		properties.setProperty("mail.imap.peek", "true");
		properties.setProperty("mail.imaps.fetchsize", "1048576");
		session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(retrive.getUserName(), retrive.getPassword());
					}
				});
		try {
			store = session.getStore("imaps");
			store.connect();
			inbox = (IMAPFolder) store.getFolder("INBOX");
			inbox.open(Folder.READ_WRITE);
			System.out.println(retrive.getEmail_set());
			Message messages[] = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			System.out.println("Number of mails = " + messages.length);
			for (int i = 0; i < messages.length; i++) {
				Message message = messages[i];
				Address[] from = message.getFrom();
				String subject = message.getSubject();
				((IMAPMessage) message).setPeek(true);
				String ee = from == null ? null : ((InternetAddress) from[0]).getAddress();
				boolean a=Arrays.asList(retrive.getEmail_list()).contains(ee);
				System.out.println(a);
				if (Arrays.asList(retrive.getEmail_set()).contains(ee)) {
					System.out.println(retrive.getEmail_set());
					notfound = false;
					System.out.println("-------------------------------");
					System.out.println("Date : " + message.getSentDate());
					System.out.println("From : " + from[0]);
					System.out.println("Subject: " + message.getSubject());
					System.out.println("Content :");
					retrive.processMessageBody(message);
					System.out.println("--------------------------------");

					recieved = LocalDateTime.now();
					mailsRepository.save(Mails.builder().body(retrive.textContent(message)).subject(message.getSubject()).m_from(ee).sent_date(message.getSentDate()).retrived_date(recieved).build());
					messages[i].setFlag(Flags.Flag.SEEN, true);

					if (!store.getFolder("processed by taxilla").exists()) {
						IMAPFolder defaultfolder = (IMAPFolder) store.getDefaultFolder();
						System.out.println("x");

						IMAPFolder newfolder = (IMAPFolder) defaultfolder.getFolder("processed by taxilla");

						newfolder.create(Folder.HOLDS_MESSAGES);

						inbox.moveMessages(new Message[]{message}, newfolder);
					}
					else {
						IMAPFolder defaultfolder = (IMAPFolder) store.getDefaultFolder();
						IMAPFolder newfolder = (IMAPFolder) defaultfolder.getFolder("processed by taxilla");
						inbox.moveMessages(new Message[]{message}, newfolder);
					}

				}

			}
			inbox.close(true);
			store.close();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		break;

	case "outlook":
		System.out.println("domain name:"+ retrive.getDomain());
		properties = new Properties();
		saveDirectory = retrive.getSaveDirectory();
		properties.setProperty("mail.imap.host", "outlook.office365.com");
		properties.setProperty("mail.imap.port", "143");
		properties.setProperty("mail.transport.protocol", "imaps");
		properties.setProperty("mail.imap.partialfetch", "false");
		properties.setProperty("mail.imap.starttls.enable", "true");
		properties.setProperty("mail.imap.peek", "true");
		properties.put("mail.imap.auth", "true");
		properties.put("mail.imap.auth.mechanisms", "XOAUTH2");
		properties.setProperty("mail.imaps.fetchsize", "1048576");
		properties.put("mail.imap.ssl.enable", "true");
		properties.put("mail.debug", "true");
		properties.put("mail.debug.auth", "true");
		String token = retrive.getAuthToken(retrive.getTenantid(),retrive.getClientid(),retrive.getClient_secret());
		session = Session.getInstance(properties,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(retrive.getUserName(), token);
					}
				});
		try {
			store = session.getStore("imaps");
			store.connect();
			inbox = (IMAPFolder) store.getFolder("INBOX");
			inbox.open(Folder.READ_WRITE);

			Message messages[] = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN),false));
			System.out.println("Number of mails = " + messages.length);

			for (int i = 0; i < messages.length; i++)
			{ Message message = messages[i];
				Address[] from = message.getFrom();
				String subject=message.getSubject();
				((IMAPMessage)message).setPeek(true);
				String ee= from==null?null:((InternetAddress)from[0]).getAddress();
				if(Arrays.asList(retrive.getEmail_set()).contains(ee)) {
					System.out.println("-------------------------------");
					System.out.println("Date : " + message.getSentDate());
					System.out.println("From : " + from[0]);
					System.out.println("Subject: " + message.getSubject());
					System.out.println("Content :");
					retrive.processMessageBody(message);
					System.out.println("--------------------------------");


					recieved = LocalDateTime.now();
					mailsRepository.save(Mails.builder().body(retrive.textContent(message)).subject(message.getSubject()).m_from(ee).sent_date(message.getSentDate()).retrived_date(recieved).build());
					messages[i].setFlag(Flags.Flag.SEEN, true);

					if (!store.getFolder("processed by taxilla").exists()) {
						IMAPFolder defaultfolder = (IMAPFolder) store.getDefaultFolder();
						System.out.println("x");

						IMAPFolder newfolder = (IMAPFolder) defaultfolder.getFolder("processed by taxilla");

						newfolder.create(Folder.HOLDS_MESSAGES);
					}
					else {
						IMAPFolder defaultfolder = (IMAPFolder) store.getDefaultFolder();
						IMAPFolder newfolder = (IMAPFolder) defaultfolder.getFolder("processed by taxilla");
						inbox.moveMessages(new Message[]{message}, newfolder);
					}

				}

			} inbox.close(true);
			store.close(); }
		catch (NoSuchProviderException e) {
			e.printStackTrace();}
		catch (MessagingException e) {
			e.printStackTrace(); }
		catch (IOException e) {
			throw new RuntimeException(e);
		}

break;
	}}
}