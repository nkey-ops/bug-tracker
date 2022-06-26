package com.bluesky.bugtraker.view.model.rensponse;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter @Setter
public class UserResponseModel {
    private String publicId;
    private String userName;
    private String email;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseModel that = (UserResponseModel) o;
        return Objects.equals(publicId, that.publicId) && Objects.equals(userName, that.userName) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicId, userName, email);
    }

    @Override
    public String toString() {
        return "UserResponseModel{" +
                "publicId='" + publicId + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
