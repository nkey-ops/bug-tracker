package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorMessages;

import java.io.Serial;

public class UserServiceException extends ServiceException {
    @Serial
    private static final long serialVersionUID = -4691731975907776276L;

    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(ErrorMessages message) {
        super(message);
    }

    public UserServiceException(ErrorMessages message, String identifier) {
        super(message, identifier);
    }
}
