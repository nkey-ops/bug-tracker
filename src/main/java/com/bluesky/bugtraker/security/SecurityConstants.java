package com.bluesky.bugtraker.security;

import com.bluesky.bugtraker.context.SpringApplicationContext;

public class SecurityConstants {
    public static final long EXPIRATION_TIME = 864_000_000L; //10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SING_UP_URL = "/users";
    public static final String VERIFICATION_EMAIL_URL = "/users/email-verification";
    public static final String LOGIN_URL = "/users/login";


    public static String getTokenSecret(){
        AppProperties appProperties = (AppProperties) SpringApplicationContext.getBean("appProperties");
        return appProperties.getTokeSecret();

    }


}