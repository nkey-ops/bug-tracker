package com.bluesky.bugtraker.configurations;

import com.bluesky.bugtraker.service.impl.EmailService;
import jakarta.servlet.annotation.WebFilter;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.datatables.repository.DataTablesRepositoryFactoryBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.thymeleaf.ITemplateEngine;

@Configuration(proxyBeanMethods = false)
@Slf4j
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
  @Profile("default")
  EmailService prodEmailService(ITemplateEngine templateEngine, JavaMailSender emailSender) {
    return new EmailService(templateEngine, emailSender);
  }

  @Bean
  @Profile("dev")
  EmailService devEmailService(ITemplateEngine templateEngine, JavaMailSender emailSender) {
    return new EmailService(templateEngine, emailSender) {
      @Override
      public void verifyEmail(@NotNull String email, @NotNull String token) {
        log.info("[Mock Call] Email " + email + " and token " + token + " were verified");
      }
    };
  }

  @Bean
  @Profile("dev")
  public HttpExchangeRepository httpTraceRepository() {
    return new InMemoryHttpExchangeRepository();
  }

  @Bean
  @Profile("dev")
  public CommonsRequestLoggingFilter logFilter() {

    @Order(value = Ordered.HIGHEST_PRECEDENCE)
    @WebFilter(filterName = "RequestCachingFilter", urlPatterns = "/*")
    class RFilter extends CommonsRequestLoggingFilter {}

    var filter = new RFilter();
    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filter.setMaxPayloadLength(10000);
    filter.setIncludeHeaders(true);
    filter.setBeforeMessagePrefix("BEFORE:" + System.lineSeparator());
    filter.setAfterMessagePrefix("AFTER: " + System.lineSeparator());
    filter.setIncludeClientInfo(true);
    return filter;
  }
}
