package com.bluesky.bugtraker.validation;

import org.springframework.beans.factory.annotation.Autowired;

import com.bluesky.bugtraker.service.UserService;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailValidator
        implements ConstraintValidator<ValidEmail, String> {
    @Autowired
    UserService userService;

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){
        return isEmailNotRegistered(email);
    }

    public boolean isEmailNotRegistered(String email){
        return !userService.isUserExistsByEmail(email);
    }
}
