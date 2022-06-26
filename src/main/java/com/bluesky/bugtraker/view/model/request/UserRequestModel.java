package com.bluesky.bugtraker.view.model.request;


import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter @Setter
public class UserRequestModel {
    private String userName;
    private String email;
    private String password;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRequestModel that = (UserRequestModel) o;
        return Objects.equals(userName, that.userName) && Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, email, password);
    }

    @Override
    public String toString() {
        return "UserRequestModel{" +
                "userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
