package com.bluesky.bugtraker.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppProperties {
    private Environment environment;

    @Autowired
    public AppProperties(Environment environment) {
        this.environment = environment;
    }

    public  String getTokeSecret(){
        return environment.getProperty("tokenSecret");
    }

    public  String getTokenRememberMe(){
        return environment.getProperty("tokenRememberMe");
    }


}
