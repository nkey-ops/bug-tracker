package com.bluesky.bugtraker.service.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotNull;
import lombok.extern.log4j.Log4j2;


@Log4j2
@Component
public class EmailServiceImpl {
	
	private final ITemplateEngine templateEngine;
	private final JavaMailSender emailSender;

	public EmailServiceImpl(ITemplateEngine templateEngine, 
							JavaMailSender emailSender) {

		this.templateEngine = templateEngine;
		this.emailSender = emailSender;
	}


	/**
	 * Verifies an email address by sending to it 
	 * a verification mail with a unique verification link 
	 * 
	 * @param email an email address to be verified
	 * @param token used to generate a unique link to verify email address
	 */
	public void verifyEmail(@NotNull String email, @NotNull String token) {
		MimeMessage message = emailSender.createMimeMessage();
		
		try {
			message.setFrom("noreply@bugtracker.com");
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject("Bug Tracker email verification");
			message.setContent(getHTMLText(token), "text/html; charset=utf-8");
			emailSender.send(message);

			log.debug(String.format("Sent a verificaton email to %s", email));

		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}

	
	/**
	 * @param token to be used as a parameter for an email verification link
	 * @return HTML page with email verification link that is built from 
	 *  current context path and token as a parameter  
	 * 
	 */
	@NotNull	
	private String getHTMLText(@NotNull String token) {
		final String baseUrl = 
				ServletUriComponentsBuilder.fromCurrentContextPath()
				.build().toUriString();

		Context context = new Context();
		
		context.setVariable("token", token);
		context.setVariable("baseURL", baseUrl);
		
		return templateEngine.process("pages/verification-email-message", context);
	}
}
