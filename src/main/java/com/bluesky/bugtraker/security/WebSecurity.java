package com.bluesky.bugtraker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class WebSecurity {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    //      @formatter:off
    return http.csrf(c -> c.disable())
        .cors(c -> c.disable())
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**", "/assets/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/login", "/signup", "/users/oath2")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/users")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, SecurityConstants.EMAIL_VERIFICATION_PROCESSING_URL)
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            l ->
                l.loginPage("/login")
                    .defaultSuccessUrl("/home", true)
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .loginProcessingUrl("/users/login")
                    .permitAll())
        .logout(l -> l.logoutUrl("/logout").invalidateHttpSession(true).deleteCookies("JSESSIONID"))
        .rememberMe(
            r ->
                r.key(SecurityConstants.getTokenRememberMe())
                    .tokenValiditySeconds(SecurityConstants.REMEMBER_ME_EXPIRATION_TIME))
        .build();
    //      @formatter:on
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

//  @Bean
//  @Profile("dev")
//  SecurityFilterChain devFilterChain(HttpSecurity http) throws Exception {
//    return http.authorizeHttpRequests(r -> r.requestMatchers("/actuator/**").permitAll()).build();
//  }
}
