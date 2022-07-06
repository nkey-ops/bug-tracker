package com.bluesky.bugtraker.security;

import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.*;

public class UserPrincipal implements UserDetails {
    @Serial
    private static final long serialVersionUID = 8772880620156304062L;
    private final UserEntity userEntity;


    private  final  String id;
    public UserPrincipal(UserEntity userEntity) {
        this.userEntity = userEntity;
        id = userEntity.getPublicId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        Set<RoleEntity> roles = userEntity.getRoles();
        if(roles == null) return grantedAuthorities;

        roles.forEach(role ->{
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getRole().name()));

                role.getAuthorities().forEach(authority ->
                        grantedAuthorities.add(new SimpleGrantedAuthority(authority.getAuthority().name())));
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


    public String getId() {
        return id;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }



    public boolean isSubscribedTo(String userId, String projectName){
        return  userEntity.getSubscribedToProjects()
                .stream()
                .anyMatch(project ->
                        project.getCreator().getPublicId().equals(userId)
                        && project.getName().equals(projectName));
    }
}
