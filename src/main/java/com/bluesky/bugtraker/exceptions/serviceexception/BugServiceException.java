package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorMessages;

import java.io.Serial;

public class BugServiceException extends ServiceException {
    @Serial
    private static final long serialVersionUID = 6578170926028564190L;

    public BugServiceException(String message) {
        super(message);
    }

    public BugServiceException(ErrorMessages message) {
        super(message);
    }

    public BugServiceException(ErrorMessages message, String identifier) {
        super(message, identifier);
    }
}
