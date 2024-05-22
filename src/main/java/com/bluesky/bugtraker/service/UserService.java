package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.shared.dto.ProjectsInfoDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.shared.dto.UserInfoDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
  UserDTO createUserWithRole(UserDTO userDto);

  UserDTO createUser(UserDTO userDto);

  boolean existsUserByEmail(@NotNull String email);

  UserDTO getUserById(String id);

  UserDTO getUserByEmail(String userName);

  DataTablesOutput<UserDTO> getUsers(DataTablesInput input);

  boolean isUserExistsByEmail(String email);

  void updateUser(String id, UserDTO userDto);

  void deleteUser(String id);

  ProjectsInfoDTO getProjectsInfo(String userId);

  UserInfoDTO getUserInfo(String userId);

  boolean isSubscribedToProject(String userId, String projectId);

  boolean isSubscribedToTicket(String userId, String ticketId);

  void verifyEmailToken(String token);
}
