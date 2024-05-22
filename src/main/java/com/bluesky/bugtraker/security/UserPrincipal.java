package com.bluesky.bugtraker.security;

import com.bluesky.bugtraker.shared.dto.UserDTO;
import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {
  @Serial private static final long serialVersionUID = 8772880620156304062L;
  private final UserDTO user;
  private final String id;

  public UserPrincipal(UserDTO user) {
    Objects.requireNonNull(user);

    this.user = user;
    this.id = user.getPublicId();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

    SimpleGrantedAuthority simpleGrantedAuthority =
        new SimpleGrantedAuthority(user.getRole().name());

    grantedAuthorities.add(simpleGrantedAuthority);

    return grantedAuthorities;
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  public String getName() {
    return user.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return user.isEmailVerificationStatus();
  }

  public String getId() {
    return id;
  }
}
