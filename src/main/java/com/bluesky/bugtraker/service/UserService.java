package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.io.entity.authorizationEntity.RoleEntity;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    UserDto getUserById(String id);
    UserDto updateUser(String id, UserDto userDto);
    UserDto createUser(UserDto userDto);
    void deleteUser(String id);

    UserDto getUserByEmail(String userName);

    UserDto createAdminUser(String email, String password, List<RoleEntity> roles);
}
