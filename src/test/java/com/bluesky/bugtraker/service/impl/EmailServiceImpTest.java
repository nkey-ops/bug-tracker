package com.bluesky.bugtraker.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.ITemplateEngine;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage; 

@ExtendWith(MockitoExtension.class)
class EmailServiceImpTest {
	
	@InjectMocks
	EmailServiceImpl emailServiceImpl;
	
	@Mock
	ITemplateEngine engine;
	
	@Mock
	JavaMailSender emailSender;

	@Test
	void verifyEmail() throws MessagingException {
		String email = "mock@email";
		String token = "mock token";

		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));	

		ArgumentCaptor<MimeMessage> captor = 
				ArgumentCaptor.forClass(MimeMessage.class);
		
		when(emailSender.createMimeMessage())
				.thenReturn(new MimeMessage((Session) null));
		
		when(engine.process(anyString(), any()))
				.thenReturn("asda");
		
		emailServiceImpl.verifyEmail(email, token);

		verify(emailSender).send(captor.capture());
		
		MimeMessage actual = captor.getValue();
		Address[] address =  actual.getAllRecipients();

		assertEquals(1, address.length);
		assertEquals(email, ((InternetAddress) address [0]).getAddress()); 
	}

}
