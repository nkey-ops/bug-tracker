package com.bluesky.bugtraker.service.utils;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.shared.dto.TicketDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.bluesky.bugtraker.service.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceUtilsTest {
    
    @InjectMocks
    private  ServiceUtils serviceUtils;
    @Mock
    private UserRepository userRepo;
    @Mock
    private ProjectRepository projectRepo;
    @Mock
    private TicketRepository ticketRepo;
    @Mock
    private RoleRepository roleRepo;
    private RoleEntity userRoleEntity;
    private UserDTO userDTO;
    private UserEntity userEntity;
    private ProjectDTO projectDTO;
    private ProjectEntity projectEntity;
    private TicketDTO ticketDTO;
    private TicketEntity ticketEntity;


    @BeforeEach
    void setUp() {
        userRoleEntity = new RoleEntity();
        userRoleEntity.setId(1L);
        userRoleEntity.setRole(Role.ROLE_USER);
       
        userDTO = new UserDTO();
        userDTO.setPublicId("1");
        userDTO.setEmail("email@sample");
        userDTO.setUsername("Username");
        userDTO.setPassword("password");
        userDTO.setAvatarURL("avatarURL");
        userDTO.setRole(userRoleEntity.getRole());
        
        userEntity = new UserEntity();
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPublicId(userDTO.getPublicId());
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setId(1L);
        userEntity.setPublicId(userDTO.getPublicId());
        userEntity.setEncryptedPassword("encryptedPassword");
        userEntity.setRoleEntity(userRoleEntity);
        
        projectDTO = new ProjectDTO();
        projectDTO.setPublicId("1");
        projectDTO.setName("projectName");
        projectDTO.setCreator(userDTO);

        projectEntity = new ProjectEntity();
        projectEntity.setId(1L);
        projectEntity.setPublicId(projectDTO.getPublicId());
        projectEntity.setName(projectDTO.getName());
        projectEntity.setCreator(userEntity);

        ticketEntity = new TicketEntity();
        ticketEntity.setId(1L);
        ticketEntity.setPublicId("1");
        ticketEntity.setShortDescription("short description");
        ticketEntity.setStatus(Status.TO_FIX);
        ticketEntity.setSeverity(Severity.CRITICAL);
        ticketEntity.setPriority(Priority.HIGH);
        ticketEntity.setHowToReproduce("how to reproduce");
        ticketEntity.setHowToSolve("Solution is not found");

        ticketDTO = new TicketDTO();
        ticketDTO.setPublicId(ticketEntity.getPublicId());
        ticketDTO.setShortDescription(ticketEntity.getShortDescription());
        ticketDTO.setStatus(ticketEntity.getStatus());
        ticketDTO.setSeverity(ticketEntity.getSeverity());
        ticketDTO.setPriority(ticketEntity.getPriority());
        ticketDTO.setHowToReproduce(ticketEntity.getHowToReproduce());
        ticketDTO.setHowToSolve(ticketEntity.getHowToSolve());
    }

    @Test
    void getUserEntity() {
        when(userRepo.findByPublicId(userEntity.getPublicId()))
                .thenReturn(Optional.of(userEntity));

        UserEntity actualUserEntity = serviceUtils.getUserEntity(userEntity.getPublicId());

        assertEqualsUser(userEntity, actualUserEntity);
    }

    @Test
    void getUserEntityThrowsWhenUserWasNotFound() {
        when(userRepo.findByPublicId(userEntity.getPublicId()))
                .thenReturn(Optional.empty());

        assertThrows(UserServiceException.class,
                () -> serviceUtils.getUserEntity(userEntity.getPublicId()));
    }

    @Test
    void getProjectEntity() {
        when(projectRepo.findByPublicId(projectEntity.getPublicId()))
                .thenReturn(Optional.of(projectEntity));

        ProjectEntity actualProjectEntity = serviceUtils.getProjectEntity(projectEntity.getPublicId());

        assertEqualsProject(projectEntity, actualProjectEntity);
    }

    @Test
    void getProjectEntityThrowsWhenProjectWasNotFound() {
        when(projectRepo.findByPublicId(projectEntity.getPublicId()))
                .thenReturn(Optional.empty());

        assertThrows(ProjectServiceException.class,
                () -> serviceUtils.getProjectEntity(projectEntity.getPublicId()));
    }

    
    @Test
    void getRoleEntityToBeSet() {
        when(roleRepo.findByRole(Role.ROLE_ADMIN)).thenReturn(Optional.of(userRoleEntity));

        RoleEntity actualRoleEntity = serviceUtils.getRoleEntityToBeSet(Role.ROLE_ADMIN, Role.ROLE_USER);

        assertEqualsRole(userRoleEntity, actualRoleEntity);
    }

    @Test
    @DisplayName("getRoleEntityToBeSet throws exception when only one super admin exists")
    void getRoleEntityToBeSetThrowsWhenOnlyOneSuperAdminExists() {
        when(userRepo.countByRoleEntityRole(Role.ROLE_SUPER_ADMIN)).thenReturn(1L);


        assertThrows(UserServiceException.class,
                () -> serviceUtils.getRoleEntityToBeSet(Role.ROLE_USER, Role.ROLE_SUPER_ADMIN));
    }
    
    @Test
    void getTicketEntity() {
        when(ticketRepo.findByPublicId(ticketEntity.getPublicId()))
                .thenReturn(Optional.of(ticketEntity));

        TicketEntity actualTicketEntity = serviceUtils.getTicketEntity(ticketEntity.getPublicId());

        assertEqualsTicket(ticketEntity, actualTicketEntity);
    }

    @Test
    void getTicketEntityThrowsWhenTicketWasNotFound() {
        when(ticketRepo.findByPublicId(ticketEntity.getPublicId()))
                .thenReturn(Optional.empty());

        assertThrows(TicketServiceException.class,
                () -> serviceUtils.getTicketEntity(ticketEntity.getPublicId()));
    }
}
