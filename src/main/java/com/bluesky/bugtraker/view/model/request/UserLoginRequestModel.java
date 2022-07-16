package com.bluesky.bugtraker.view.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserLoginRequestModel {
    private String email;
    private String password;

    public UserLoginRequestModel(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
