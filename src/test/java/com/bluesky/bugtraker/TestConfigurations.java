package com.bluesky.bugtraker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.bluesky.bugtraker.service.impl.EmailServiceImpl;

import jakarta.validation.constraints.NotNull;


@TestConfiguration
public class TestConfigurations {

	Logger log = LogManager.getLogger(TestConfigurations.class); 

	@Bean
	@Primary
	EmailServiceImpl emailServiceImpl(
							SpringTemplateEngine templateEngine, 
							JavaMailSender emailSender) {

	return new EmailServiceImpl(templateEngine, emailSender) {

		@Override
		public void verifyEmail(@NotNull String email, @NotNull String token) {
			log.info("Email "  + email + " and token " + token  + " were verified"); 	
		}};
	}
}
