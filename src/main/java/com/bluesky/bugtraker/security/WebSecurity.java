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
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
public class WebSecurity {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

//      @formatter:off
        return http
                .cors(withDefaults())
                .csrf( csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .antMatchers("/webjars/**", "/css/**", "/js/**", "/images/**", "/assets/**")
                        .permitAll()
                        .antMatchers(HttpMethod.GET, "/login", "/signup", 
                        		SecurityConstants.VERIFICATION_EMAIL_URL,
                        		"/verification/email",
                        		"/email-verification")
                        
                        .permitAll()
                        .antMatchers(HttpMethod.POST, "/users")
                        .permitAll()

                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .loginProcessingUrl(SecurityConstants.VERIFICATION_LOGIN_URL)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .rememberMe(me -> me
                        .key(SecurityConstants.getTokenRememberMe())
                        .tokenValiditySeconds(SecurityConstants.REMEMBER_ME_EXPIRATION_TIME))
                .build();
                
//      @formatter:on
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

}