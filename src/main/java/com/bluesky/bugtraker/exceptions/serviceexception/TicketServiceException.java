package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorMessages;

import java.io.Serial;

public class TicketServiceException extends ServiceException {
    @Serial
    private static final long serialVersionUID = 6578170926028564190L;

    public TicketServiceException(String message) {
        super(message);
    }

    public TicketServiceException(ErrorMessages message) {
        super(message);
    }

    public TicketServiceException(ErrorMessages message, String identifier) {
        super(message, identifier);
    }
}
