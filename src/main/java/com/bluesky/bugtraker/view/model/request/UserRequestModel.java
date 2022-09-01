package com.bluesky.bugtraker.view.model.request;


import com.bluesky.bugtraker.validation.ValidEmail;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Getter @Setter
public class UserRequestModel {
    @NotEmpty
    @Size(min = 4 , message = "Length should be not less than 4 characters")
    private String username;
    @NotEmpty
    @ValidEmail
    private String email;
    @NotEmpty
    private String password;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRequestModel that = (UserRequestModel) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }
}
