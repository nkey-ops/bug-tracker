package com.bluesky.bugtraker.exceptions;

import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ServiceExceptionAdvice {
  Logger logger = LoggerFactory.getLogger(ServiceExceptionAdvice.class);

  @ResponseBody
  @ExceptionHandler({ServiceException.class})
  public ResponseEntity<?> handleException(ServiceException ex) {

    HttpStatus httpStatus =
        switch (ex.getErrorType()) {
          case MISSING_REQUIRED_FIELD -> HttpStatus.BAD_REQUEST;
          case NO_RECORD_FOUND -> HttpStatus.NOT_FOUND;
          case AUTHENTICATION_FAILED, EMAIL_ADDRESS_NOT_VERIFIED -> HttpStatus.FORBIDDEN;
          case COULD_NOT_DELETE_RECORD, COULD_NOT_UPDATE_RECORD -> HttpStatus.NOT_MODIFIED;
          default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

    logger.error(
        ex.getErrorType().getText() + ": " + ex.getErrorMessage(), (Object[]) ex.getStackTrace());
    return new ResponseEntity<>(
        ex.getErrorType().getText() + ": " + ex.getErrorMessage(), httpStatus);
  }

  @ResponseBody
  @ExceptionHandler(BindException.class)
  public ResponseEntity<?> handleException(BindException ex) {
    final List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

    logger.error(fieldErrors.get(0).getDefaultMessage());
    return ResponseEntity.badRequest().body(fieldErrors.get(0).getDefaultMessage());
  }
}
