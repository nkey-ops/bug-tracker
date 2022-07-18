package com.bluesky.bugtraker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableWebSecurity
public class WebSecurity {

    private final CustomDsl customDsl;

    public WebSecurity(CustomDsl customDsl) {
        this.customDsl = customDsl;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return  http
                .cors().and()
                .csrf().disable()

                .authorizeRequests()
                .antMatchers("/webjars/**", "/css/**", "/js/**", "/images/**", "/assets/**")
                .permitAll()
                .antMatchers("/login", "/signup")
                .permitAll()

                .antMatchers(HttpMethod.POST, "/users")
                .permitAll()
                .antMatchers(HttpMethod.GET, SecurityConstants.VERIFICATION_EMAIL_URL)
                .permitAll()

                .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .loginProcessingUrl(SecurityConstants.VERIFICATION_LOGIN_URL)
                        .defaultSuccessUrl("/home", true)
                .permitAll()
                .and()
                .logout().deleteCookies("JSESSIONID")
                .and()
                .rememberMe()
                    .key(SecurityConstants.getTokenRememberMe())
                    .tokenValiditySeconds(SecurityConstants.REMEMBER_ME_EXPIRATION_TIME)

                .and().apply(customDsl)
//                TODO try to find how to solve the problem with stateless session
//                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().build();

    }
}

//@EnableWebSecurity
//public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//                .antMatchers( "/login/**", "/signup")
//                .permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .formLogin()
//                    .loginPage("/login")
//                        .usernameParameter("email")
//                        .passwordParameter("password")
//                        .loginProcessingUrl(SecurityConstants.VERIFICATION_LOGIN_URL)
//                        .defaultSuccessUrl("/home")
//                .permitAll();
//    }
//
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/webjars/**", "/css/**", "/js/**", "/images/**", "/assets/**");
//    }
//}
