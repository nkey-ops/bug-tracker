package com.bluesky.bugtraker.view.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter @Setter
public class UserLoginRequestModel {
    @NotNull
    @NotEmpty
    private String email;
    @NotNull
    @NotEmpty
    private String password;

    public UserLoginRequestModel() {
    }

    public UserLoginRequestModel(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
