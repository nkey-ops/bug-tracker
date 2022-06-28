package com.bluesky.bugtraker.exceptions;

public class UserNotFoundException extends  RuntimeException{

    public UserNotFoundException(String id) {
        super("User hasn't been found with " + id);
    }

}
