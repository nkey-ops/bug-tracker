package com.bluesky.bugtraker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
public class WebSecurity {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//      @formatter:off
        return http
                .cors()
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/webjars/**", "/css/**", "/js/**", "/images/**", "/assets/**")
                .permitAll()
                .antMatchers(HttpMethod.GET, "/login", "/signup", "/users/oath2")
                .permitAll()
                .antMatchers(HttpMethod.POST, "/users")
                .permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.VERIFICATION_EMAIL_URL)
                .permitAll()

                .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/home", true)
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .loginProcessingUrl("/users/login")
                .permitAll()
                .and()
                .logout()
                    .logoutUrl("/logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                .and()
                .rememberMe()
                    .key(SecurityConstants.getTokenRememberMe())
                    .tokenValiditySeconds(SecurityConstants.REMEMBER_ME_EXPIRATION_TIME)
                .and()
                .build();
                
//                .exceptionHandling(e -> e
//                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
//                )
//                      .defaultSuccessUrl("/home", true)
//                    .loginProcessingUrl("/users/login")
//                .and()
//                .formLogin(e -> e
//                        .defaultSuccessUrl("/home", true)
//                    .usernameParameter("email")
//                    .passwordParameter("password")
//                    .loginProcessingUrl("/login/github/authorized")
//                )
//                .and()
//                    .oauth2Login()
//                        .loginPage("/login")
//                    .redirectionEndpoint()
//                        .baseUri("/home")
//                .and()
//                .build();
        
//      @formatter:on
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}