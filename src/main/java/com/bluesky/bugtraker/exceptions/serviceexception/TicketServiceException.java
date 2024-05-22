package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorType;
import java.io.Serial;

public class TicketServiceException extends ServiceException {
  @Serial private static final long serialVersionUID = 6578170926028564190L;

  public TicketServiceException(String message) {
    super(message);
  }

  public TicketServiceException(ErrorType message) {
    super(message);
  }

  public TicketServiceException(ErrorType message, String identifier) {
    super(message, identifier);
  }
}
