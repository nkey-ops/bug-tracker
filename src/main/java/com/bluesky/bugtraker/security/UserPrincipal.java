package com.bluesky.bugtraker.security;

import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserPrincipal implements UserDetails {
    @Serial
    private static final long serialVersionUID = 8772880620156304062L;
    private final UserDto user;
    private  final  String id;
    public UserPrincipal(UserDto user) {
        this.user = user;
        this.id = user.getPublicId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        Set<RoleEntity> roles = user.getRoles();
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
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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



    public boolean isSubscribedTo(String projectCreatorId, String projectName){
        return  user.getSubscribedToProjects()
                .stream()
                .anyMatch(project ->
                        project.getCreator().getPublicId().equals(projectCreatorId)
                        && project.getName().equals(projectName));
    }
}
