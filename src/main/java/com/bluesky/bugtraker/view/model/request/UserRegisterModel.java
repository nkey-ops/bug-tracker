package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.validation.ValidEmail;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterModel {
  @NotEmpty
  @Size(min = 4, message = "Length should be not less than 4 characters")
  private String username;

  @NotEmpty @ValidEmail private String email;
  @NotEmpty private String password;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserRegisterModel that = (UserRegisterModel) o;
    return Objects.equals(email, that.email) && Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, password);
  }
}
