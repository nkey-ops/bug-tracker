package com.bluesky.bugtraker.configurations;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.ITemplateEngine;

import com.bluesky.bugtraker.service.impl.EmailService;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(
    repositoryFactoryBeanClass = DataTablesRepositoryFactoryBean.class,
    basePackages = "com.bluesky.bugtraker.io.repository")
public class Configurations {
  @Bean
  ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());

    return modelMapper;
  }

  @Bean
  @Profile("prod")
  EmailService emailService(ITemplateEngine templateEngine, JavaMailSender emailSender) {
    return new EmailService(templateEngine, emailSender);
  }
}
