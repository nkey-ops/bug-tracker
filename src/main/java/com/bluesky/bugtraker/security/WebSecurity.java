package com.bluesky.bugtraker.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

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
                        .antMatchers(HttpMethod.GET, 
                        		"/login", "/signup", "/verification/email",
                        		SecurityConstants.EMAIL_VERIFICATION_PROCESSING_URL)
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
                        .failureHandler(authenticationFailureHandler())
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

	SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
		return new SimpleUrlAuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {

				if (exception instanceof DisabledException)
					super.setDefaultFailureUrl("/login?verificaton=false");
				else
					super.setDefaultFailureUrl("/login?error");

				super.onAuthenticationFailure(request, response, exception);
			}

		};
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

}