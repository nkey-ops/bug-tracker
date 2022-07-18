package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface UserService extends UserDetailsService {
    UserDto createAdminUser(String email, String password, Set<RoleEntity> roles);

    UserDto createUser(UserDto userDto);

    UserDto getUserById(String id);

    UserDto getUserByEmail(String userName);

    boolean isUserExistsByEmail(String email);


    UserDto updateUser(String id, UserDto userDto);

    void deleteUser(String id);

    Set<BugDto> getReportedBugs(String id, int page, int limit);

    Set<BugDto> getGetWorkingOnBugs(String id, int page, int limit);

    Set<ProjectDto> getSubscribedProjects(String id, int page, int limit);
}
