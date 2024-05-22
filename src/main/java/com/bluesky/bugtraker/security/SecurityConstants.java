package com.bluesky.bugtraker.security;

import com.bluesky.bugtraker.context.SpringApplicationContext;

public class SecurityConstants {

  private static final AppProperties appProperties =
      (AppProperties) SpringApplicationContext.getBean("appProperties");

  public static final long EXPIRATION_TIME = 864_000_000L; // 10 days
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String HEADER_STRING = "Authorization";
  public static final String SING_UP_URL = "/signup";
  public static final String EMAIL_VERIFICATION_PROCESSING_URL = "/users/email-verification";
  public static final String VERIFICATION_LOGIN_URL = "/users/login";
  public static final int REMEMBER_ME_EXPIRATION_TIME = 604_800; // 1 week

  public static String getTokenSecret() {
    return appProperties.getTokeSecret();
  }

  public static String getTokenRememberMe() {
    return appProperties.getTokenRememberMe();
  }
}
