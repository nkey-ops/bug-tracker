package com.bluesky.bugtraker.exceptions;

import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@ControllerAdvice
public class ServiceExceptionAdvice {

    @ResponseBody
    @ExceptionHandler({ServiceException.class})
    public ResponseEntity<?> handleException(ServiceException ex) {

        HttpStatus httpStatus = switch (ex.getErrorType()) {
            case MISSING_REQUIRED_FIELD -> HttpStatus.BAD_REQUEST;
            case NO_RECORD_FOUND -> HttpStatus.NOT_FOUND;
            case AUTHENTICATION_FAILED, EMAIL_ADDRESS_NOT_VERIFIED -> HttpStatus.FORBIDDEN;
            case COULD_NOT_DELETE_RECORD, COULD_NOT_UPDATE_RECORD -> HttpStatus.NOT_MODIFIED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<> (ex.getErrorType().getText() + ": " +  ex.getErrorMessage(), httpStatus);
    }

    @ResponseBody
    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleException(BindException ex) {
        final List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        
        return ResponseEntity.badRequest().body(fieldErrors.get(0).getDefaultMessage());
    }
    
}