package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorType;

import java.io.Serial;

public class ProjectServiceException extends ServiceException {
    @Serial
    private static final long serialVersionUID = -8090704444250043231L;

    public ProjectServiceException(String message) {
        super(message);
    }

    public ProjectServiceException(ErrorType message) {
        super(message);
    }

    public ProjectServiceException(ErrorType message, String identifier) {
        super(message, identifier);
    }
}
