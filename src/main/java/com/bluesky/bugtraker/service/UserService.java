package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface UserService extends UserDetailsService {

    UserDto getUserById(String id);
    UserDto updateUser(String id, UserDto userDto);
    UserDto createUser(UserDto userDto);
    void deleteUser(String id);

    UserDto getUserByEmail(String userName);

    UserDto createAdminUser(String email, String password, Set<RoleEntity> roles);
}
