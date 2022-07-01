package com.bluesky.bugtraker.shared.dto;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
public class UserDto implements Serializable {
    private Long id;
    private String publicId;
    private String userName;
    private String email;
    private String password;

    private Set<ProjectDto> userProjects;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(publicId, userDto.publicId) && Objects.equals(userName, userDto.userName) && Objects.equals(email, userDto.email) && Objects.equals(password, userDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicId, userName, email, password);
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "publicId='" + publicId + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
