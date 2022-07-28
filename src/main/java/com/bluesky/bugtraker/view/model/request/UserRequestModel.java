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
    @NotNull
    @NotEmpty
    @Size(min = 4)
    private String username;
    @NotNull
    @NotEmpty
    @ValidEmail
    private String email;
    @NotNull
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
