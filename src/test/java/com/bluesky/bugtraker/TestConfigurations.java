package com.bluesky.bugtraker;

import com.bluesky.bugtraker.service.impl.EmailService;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.spring6.SpringTemplateEngine;

@TestConfiguration
public class TestConfigurations {

  Logger log = LogManager.getLogger(TestConfigurations.class);

  @Bean
  JavaMailSender mockJavaMailSender() {
    return new JavaMailSenderImpl();
  }

  @Bean
  EmailService testEmailService(JavaMailSender jMailSenderl) {

    return new EmailService(new SpringTemplateEngine(), jMailSenderl) {
      @Override
      public void verifyEmail(@NotNull String email, @NotNull String token) {
        log.info("[Mock Call] Email " + email + " and token " + token + " were verified");
      }
    };
  }
}
