package com.bluesky.bugtraker.view.model.rensponse;

import com.bluesky.bugtraker.shared.authorizationenum.Role;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
public class UserResponseModel extends RepresentationModel<UserResponseModel> {
  private String publicId;
  private String username;
  private String email;

  private String avatarURL;
  private String address;
  private String phoneNumber;
  private String status;
  private Role role;

  public UserResponseModel(String username) {
    this.username = username;
  }

  public UserResponseModel() {}

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserResponseModel that = (UserResponseModel) o;
    return Objects.equals(publicId, that.publicId)
        && Objects.equals(username, that.username)
        && Objects.equals(email, that.email)
        && Objects.equals(avatarURL, that.avatarURL)
        && Objects.equals(address, that.address)
        && Objects.equals(phoneNumber, that.phoneNumber)
        && Objects.equals(status, that.status)
        && role == that.role;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(), publicId, username, email, avatarURL, address, phoneNumber, status, role);
  }
}
