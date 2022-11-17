package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorType;

import java.io.Serial;

public class UserServiceException extends ServiceException {
    @Serial
    private static final long serialVersionUID = -4691731975907776276L;

    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(ErrorType message) {
        super(message);
    }

    public UserServiceException(ErrorType message, String identifier) {
        super(message, identifier);
    }
}
