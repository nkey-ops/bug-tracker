package com.bluesky.bugtraker.security;

import com.bluesky.bugtraker.security.filter.AuthenticationFilter;
import com.bluesky.bugtraker.security.filter.AuthorizationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import javax.servlet.Filter;

public class CustomDsl extends AbstractHttpConfigurer<CustomDsl, HttpSecurity> {

    public static CustomDsl customDsl() {
        return new CustomDsl();
    }

    @Override
    public void configure(HttpSecurity http)  {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http
                .addFilter(getAuthenticationFilter(authenticationManager))
                .addFilter(getAuthorizationFilter(authenticationManager));
    }


    private Filter getAuthorizationFilter(AuthenticationManager authenticationManager) {
        return new AuthorizationFilter(authenticationManager);
    }

    public AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager){
        AuthenticationFilter filter =new AuthenticationFilter(authenticationManager);
        filter.setFilterProcessesUrl("/users/login");
        return filter;
    }

}
