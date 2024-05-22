package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorType;
import java.io.Serial;

public class RoleServiceException extends ServiceException {
  @Serial private static final long serialVersionUID = 949632281436845943L;

  public RoleServiceException(String message) {
    super(message);
  }

  public RoleServiceException(ErrorType message) {
    super(message);
  }

  public RoleServiceException(ErrorType message, String identifier) {
    super(message, identifier);
  }
}
