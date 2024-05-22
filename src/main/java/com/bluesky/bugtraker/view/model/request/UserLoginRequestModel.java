package com.bluesky.bugtraker.view.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequestModel {
  @NotNull @NotEmpty private String email;
  @NotNull @NotEmpty private String password;

  public UserLoginRequestModel() {}

  public UserLoginRequestModel(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
