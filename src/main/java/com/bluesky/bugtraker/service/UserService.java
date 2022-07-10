package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface UserService extends UserDetailsService {
    UserDto createAdminUser(String email, String password, Set<RoleEntity> roles);

    UserDto createUser(UserDto userDto);

    UserDto getUserById(String id);

    UserDto getUserByEmail(String userName);

    UserDto updateUser(String id, UserDto userDto);

    void deleteUser(String id);

//    @Deprecated
//    void addProjectToUser(String userId, ProjectDto projectDto);
//
//    @Deprecated
//    void removeProject(String userId, ProjectDto projectDto);
}
