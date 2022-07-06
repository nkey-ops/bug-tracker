package com.bluesky.bugtraker.exceptions;

import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class ServiceExceptionAdvice {

    @ResponseBody
    @ExceptionHandler(ServiceException.class)
    ResponseEntity<?> handleException(ServiceException ex) {

        HttpStatus httpStatus = switch (ex.getErrorType()) {
            case MISSING_REQUIRED_FIELD -> HttpStatus.BAD_REQUEST;
            case NO_RECORD_FOUND -> HttpStatus.NOT_FOUND;
            case AUTHENTICATION_FAILED, EMAIL_ADDRESS_NOT_VERIFIED -> HttpStatus.FORBIDDEN;
            case COULD_NOT_DELETE_RECORD, COULD_NOT_UPDATE_RECORD -> HttpStatus.NOT_MODIFIED;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", httpStatus.value());
        body.put("error", ex.getMessage());

        return new ResponseEntity<>(body, httpStatus);
    }
}
