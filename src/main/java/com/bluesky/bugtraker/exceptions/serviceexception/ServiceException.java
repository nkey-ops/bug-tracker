package com.bluesky.bugtraker.exceptions.serviceexception;

import com.bluesky.bugtraker.exceptions.ErrorType;
import java.io.Serial;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class ServiceException extends RuntimeException {

  @Serial
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private static final long serialVersionUID = 1181223944376197491L;

  @Getter private ErrorType errorType;
  @Getter private String errorMessage;

  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(ErrorType errorType) {
    super(errorType.getText());
    this.errorType = errorType;
  }

  public ServiceException(ErrorType errorType, String errorMessage) {
    super(errorType.toString() + ": " + errorMessage);
    this.errorType = errorType;
    this.errorMessage = errorMessage;
  }
}
