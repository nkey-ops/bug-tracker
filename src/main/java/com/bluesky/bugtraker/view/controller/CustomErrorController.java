package com.bluesky.bugtraker.view.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return "/errors/error-404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "/errors/error-500";
            }
        }

        

        int i = Integer.parseInt(status.toString());
        return "/errors/error :: simpleError (errorMessage='" + i + "')";
    }
}