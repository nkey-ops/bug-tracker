package com.bluesky.bugtraker.service.impl;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;

import lombok.extern.log4j.Log4j2;


@Log4j2
@Component
public class EmailServiceImpl {
	
	@Autowired
	private SpringTemplateEngine templateEngine;
	
	@Autowired
	private ServletContext sContext;

	@Autowired
	private JavaMailSender emailSender;



	public void verifyEmail(String email, String token) {
		MimeMessage message = emailSender.createMimeMessage();
		
		
		try {
			message.setFrom("noreply@bugtracker.com");
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject("Bug Tracker email verification");
			message.setContent(getHTMLText(token), "text/html; charset=utf-8");
			emailSender.send(message);

		log.info(String.format("Sent an verificaton email to %s with token %s", email, token));

		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}
	
	public String getHTMLText(String token) {
		final String baseUrl = 
				ServletUriComponentsBuilder.fromCurrentContextPath()
				.build().toUriString();

		Context context = new Context();
		
		context.setVariable("token", token);
		context.setVariable("baseURL", baseUrl);
		
		return templateEngine.process("pages/verification-email", context);
	}
}
