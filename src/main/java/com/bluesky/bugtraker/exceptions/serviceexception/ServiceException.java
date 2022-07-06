package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorMessages;
import lombok.Getter;

import java.io.Serial;

public abstract class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1181223944376197491L;

    @Getter
    private ErrorMessages errorType;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(ErrorMessages message) {
        super(message.toString());
        this.errorType = message;
    }

    public ServiceException(ErrorMessages message, String identifier) {
        super(message.toString() + ": " + identifier);
        this.errorType = message;
    }

}
