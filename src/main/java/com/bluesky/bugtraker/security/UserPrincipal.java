package com.bluesky.bugtraker.security;

import com.bluesky.bugtraker.io.entity.authorizationEntity.RoleEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
    @Serial
    private static final long serialVersionUID = 8772880620156304062L;
    private final UserEntity userEntity;

    public UserPrincipal(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        List<RoleEntity> roles = userEntity.getRoles();
        if(roles == null) return grantedAuthorities;

        roles.forEach(role ->{
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));

                role.getAuthorities().forEach(authority ->
                        grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName())));
        });

        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return userEntity.getEncryptedPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
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
        return true;
    }
}
