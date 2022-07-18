package com.bluesky.bugtraker.security;

import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.security.filter.AuthenticationFilter;
import com.bluesky.bugtraker.security.filter.AuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;

@Component
public class CustomDsl extends AbstractHttpConfigurer<CustomDsl, HttpSecurity> {

    private  final UserRepository userRepository;

    @Autowired
    public CustomDsl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public void configure(HttpSecurity http)  {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http
                .addFilter(getAuthenticationFilter(authenticationManager))
                .addFilter(getAuthorizationFilter(authenticationManager));
    }


    private Filter getAuthorizationFilter(AuthenticationManager authenticationManager) {
        return new AuthorizationFilter(authenticationManager, userRepository);
    }

    public AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager){
        AuthenticationFilter filter =new AuthenticationFilter(authenticationManager);
        filter.setFilterProcessesUrl(SecurityConstants.VERIFICATION_LOGIN_URL);
        filter.setPostOnly(true);
        return filter;
    }

}
