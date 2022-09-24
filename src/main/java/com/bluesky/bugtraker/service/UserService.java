package com.bluesky.bugtraker.service;


import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Set;

public interface UserService extends UserDetailsService  {
    UserDto createUserWithRoles(UserDto userDto, Set<Role> roles);

    UserDto createUser(UserDto userDto);

    UserDto getUserById(String id);

    UserDto getUserByEmail(String userName);

    boolean isUserExistsByEmail(String email);


    void updateUser(String id, UserDto userDto);

    void deleteUser(String id);

    Page<TicketDto> getReportedTickets(String id, int page, int limit);

    Page<TicketDto> getWorkingOnTickets(String id, int page, int limit);

    Page<ProjectDto> getSubscribedOnProjects(String id, int page, int limit);

    TicketsInfoDTO getTicketsInfo(String userId);

    UserInfoDTO getUserInfo(String userId);
}
