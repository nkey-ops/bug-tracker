package com.bluesky.bugtraker.view.model.request;

import com.bluesky.bugtraker.shared.authorizationenum.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class UserRequestModel {
  @Size(
      min = 4,
      max = 20,
      message = "Username length should be not less than 4 characters and no more than 20")
  private String username;

  @Size(max = 100, message = "Avatar URL length should be no more than 100")
  @URL(protocol = "https", host = "i.imgur.com", message = "From https://i.imgur.com")
  private String avatarUrl;

  @Size(max = 60, message = "Address length should be no more than 60")
  private String address;

  @Size(max = 12, message = "Phone number length should be no more than 12")
  private String phoneNumber;

  @Size(max = 12, message = "Status length should be no more than 12")
  private String status;

  private Role role;

  @Override
  public String toString() {
    return "UserRequestModel [username="
        + username
        + ", avatarURL="
        + avatarUrl
        + ", address="
        + address
        + ", phoneNumber="
        + phoneNumber
        + ", status="
        + status
        + ", role="
        + role
        + "]";
  }
}
