package com.bluesky.bugtraker.shared.dto;

import com.bluesky.bugtraker.shared.authorizationenum.Role;
import java.io.Serial;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleDto implements Serializable {
  @Serial
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private static final long serialVersionUID = -8715267097779379646L;

  private Role role;
}
