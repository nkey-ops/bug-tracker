package com.bluesky.bugtraker.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String email) {
        super("User with such an email already exists " + email);
    }
}
