package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.RoleServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.entity.authorization.RoleEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.Utils;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImpTest {

    private AutoCloseable closeable;

    @InjectMocks
    private UserServiceImp userServiceImp;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TicketRepository bugRepo;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private Utils utils;
    @Mock
    private ModelMapper modelMapper;

    UserEntity outputUserEntity;
    UserDto inputUserDto;
    UserDto outputUserDto;
    String userEmail;
    Long userId;
    String userPublicId;
    String userName;
    String userPassword;
    String userEncryptedPassword;


    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        userEmail = "email@sample";
        userId = 12525252352L;
        userPublicId = "adadasdas";
        userName = "Name";
        userPassword = "adasdasda";
        userEncryptedPassword = "asdasdad1121d";

        inputUserDto = new UserDto();
        inputUserDto.setEmail(userEmail);
        inputUserDto.setUsername(userName);
        inputUserDto.setPassword(userPassword);

        outputUserDto = inputUserDto;
        outputUserDto.setRoles(getSetOfRoleEntities());
        outputUserDto.setPublicId(userPublicId);
        outputUserDto.setId(userId);
        outputUserDto.setPassword(userEncryptedPassword);

        outputUserEntity = new UserEntity();
        outputUserEntity.setEmail(userEmail);
        outputUserEntity.setPublicId(userPublicId);
        outputUserEntity.setUsername(userName);
        outputUserEntity.setId(userId);
        outputUserEntity.setEncryptedPassword(userEncryptedPassword);


        outputUserEntity.setRoles(getSetOfRoleEntities());
    }

    @AfterEach
    public final void releaseMocks() throws Exception {
        closeable.close();
    }

    private Set<RoleEntity> getSetOfRoleEntities() {
        RoleEntity roleEntity = new RoleEntity(Role.ROLE_USER);
        return Set.of(roleEntity);
    }

    @Test
    void isUserExistsByEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        assertTrue(userServiceImp.isUserExistsByEmail(userEmail));


        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        assertFalse(userServiceImp.isUserExistsByEmail(userEmail));
    }

    @Test
    void getUserById() {
        when(userRepository.findByPublicId(anyString())).thenReturn(Optional.of(outputUserEntity));
        when(modelMapper.map(any(UserEntity.class), eq(UserDto.class))).thenReturn(outputUserDto);

        UserDto userById = userServiceImp.getUserById(outputUserEntity.getPublicId());
        assertEqualsUserDto(outputUserDto, userById);
    }

    @Test
    void getUserById_NullPointerException() {
        assertThrows(NullPointerException.class,
                () -> userServiceImp.getUserById(null));
    }

    @Test
    void getUserById_DoesntThrow() {
        when(userRepository.findByPublicId(anyString())).thenReturn(Optional.of(outputUserEntity));
        assertDoesNotThrow(() -> userServiceImp.getUserById(userPublicId));
    }


    @Test
    void getUserById_UserServiceException() {
        when(userRepository.findByPublicId(anyString())).thenReturn(Optional.empty());

        assertThrows(UserServiceException.class,
                () -> userServiceImp.getUserById("id"));
    }

    @Test
    void getUserByEmail() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(outputUserEntity));
        when(modelMapper.map(any(UserEntity.class), eq(UserDto.class)))
                .thenReturn(outputUserDto);


        UserDto userByEmail = userServiceImp.getUserByEmail(userEmail);

        assertEqualsUserDto(outputUserDto, userByEmail);
    }

    @Test
    void getUserByEmail_UserServiceException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserServiceException.class,
                () -> userServiceImp.getUserByEmail(userEmail));
    }

    @Test
    void getUserByEmail_NullPointerException() {
        assertThrows(NullPointerException.class,
                () -> userServiceImp.getUserByEmail(null));
    }

    @Test
    void createUserWithRoles() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(utils.generateUserId(anyInt())).thenReturn(userPublicId);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(userEncryptedPassword);

        when(modelMapper.map(any(UserDto.class), eq(UserEntity.class)))
                .thenReturn(outputUserEntity);

        when(roleRepository.findAllByRoleIn(anySet()))
                .thenReturn(Optional.of(getSetOfRoleEntities()));
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(outputUserEntity);
        when(modelMapper.map(any(UserEntity.class), eq(UserDto.class)))
                .thenReturn(outputUserDto);

        UserDto userResult = userServiceImp.createUserWithRoles(inputUserDto, Set.of(Role.ROLE_USER));

        assertEqualsUserDto(outputUserDto, userResult);

        verify(utils, times(1)).generateUserId(anyInt());
        verify(bCryptPasswordEncoder, times(1)).encode(anyString());
        verify(roleRepository, times(1)).findAllByRoleIn(anySet());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void createUserWithRoles_UserServiceException() {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);
        when(modelMapper.map(any(UserDto.class), eq(UserEntity.class)))
                .thenReturn(outputUserEntity);

        assertThrows(UserServiceException.class,
                () -> userServiceImp.createUserWithRoles(inputUserDto, Set.of(Role.ROLE_USER)));
    }

    @Test
    void createUserWithRoles_RoleServiceException() {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(modelMapper.map(any(UserDto.class), eq(UserEntity.class)))
                .thenReturn(outputUserEntity);
        when(roleRepository.findAllByRoleIn(anySet()))
                .thenReturn(Optional.empty());

        assertThrows(RoleServiceException.class, () -> {
            userServiceImp.createUserWithRoles(inputUserDto, Set.of(Role.ROLE_USER));
        });
    }

    @Test
    void createUserWithRoles_NullPointerException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->
                        userServiceImp.createUserWithRoles(null, Set.of(Role.ROLE_USER))),
                () -> assertThrows(NullPointerException.class, () ->
                        userServiceImp.createUserWithRoles(inputUserDto, null))
        );
    }

    private void assertEqualsUserDto(UserDto expected, UserDto result) {
        assertAll(
                () -> assertNotNull(result),
                () -> assertNotNull(result.getId()),
                () -> assertNotNull(result.getPublicId()),
                () -> assertNotNull(result.getRoles()),

                () -> assertTrue(result.getProjects().isEmpty()),
                () -> assertTrue(result.getReportedTickets().isEmpty()),
                () -> assertTrue(result.getWorkingOnTickets().isEmpty()),
                () -> assertTrue(result.getSubscribedToProjects().isEmpty()),

                () -> assertEquals(expected.getRoles(), result.getRoles()),
                () -> assertEquals(expected.getEmail(), result.getEmail()),
                () -> assertEquals(expected.getUsername(), result.getUsername()),
                () -> assertEquals(expected.getPassword(), result.getPassword()));
    }

    @Test
    void createUser() {
        UserServiceImp spy = spy(userServiceImp);

        Mockito.doReturn(outputUserDto)
                .when(spy)
                .createUserWithRoles(any(UserDto.class), anySet());

        UserDto resultUserDto =
                spy.createUser(inputUserDto);

        assertEqualsUserDto(outputUserDto, resultUserDto);

    }

    @Test
    void updateUser() {
        when(userRepository.findByPublicId(anyString()))
                .thenReturn(Optional.ofNullable(outputUserEntity));
        when(userRepository.save(any(UserEntity.class)))
                .thenReturn(outputUserEntity);
        when(modelMapper.map(any(UserEntity.class), eq(UserDto.class)))
                .thenReturn(outputUserDto);

        userServiceImp.updateUser(userPublicId, inputUserDto);

    }

    @Test
    void deleteUser() {
        when(userRepository.existsByPublicId(anyString()))
                .thenReturn(true);
        when(userRepository.findByPublicId(anyString()))
                .thenReturn(Optional.of(outputUserEntity));

        assertDoesNotThrow(() ->
                userServiceImp.deleteUser(userPublicId));

        verify(userRepository, times(1)).existsByPublicId(anyString());
        verify(userRepository, times(1)).delete(any(UserEntity.class));
    }

    @Test
    void deleteUser_UserServiceException() {
        when(userRepository.existsByPublicId(anyString()))
                .thenReturn(false);


        assertThrows(UserServiceException.class, () -> {
            userServiceImp.deleteUser(userPublicId);
        });
    }

    @Test
    void deleteUser_NullPointerException() {
        assertThrows(NullPointerException.class,
                () -> userServiceImp.deleteUser(null));

    }


    @Test
    void getReportedTickets() {
        int page = 1;
        int limit = 1;

        List<TicketEntity> ticketEntities = List.of(new TicketEntity());

        PageImpl<TicketEntity> pagedTicketEntities =
                new PageImpl<>(ticketEntities,
                        Pageable.ofSize(page), limit);

        List<TicketDto> ticketDtos = List.of(new TicketDto());

        PageImpl<TicketDto> outputPagedTicketDtos =
                new PageImpl<>(ticketDtos,
                        Pageable.ofSize(page), limit);


        when(userRepository.findByPublicId(anyString()))
                .thenReturn(Optional.of(outputUserEntity));
        when(bugRepo.findAllByReporter(any(UserEntity.class), any(Pageable.class)))
                .thenReturn(pagedTicketEntities);
        when(modelMapper.map(
                ArgumentMatchers.<Page<TicketEntity>>any(),
                eq(new TypeToken<Page<TicketDto>>() {
                }.getType())))
                .thenReturn(outputPagedTicketDtos);


        Page<TicketDto> reportedTickets =
                userServiceImp.getReportedTickets(userPublicId, page, limit);


        assertNotNull(reportedTickets);

        assertEquals(ticketEntities.size(), reportedTickets.getTotalElements());
        assertEquals(0, reportedTickets.getNumber());
        assertEquals(limit, reportedTickets.getSize());
    }

    @Test
    void getReportedTickets_Exceptions() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->
                        userServiceImp.getReportedTickets(null, 1, 1)),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        userServiceImp.getReportedTickets(userPublicId, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        userServiceImp.getReportedTickets(userPublicId, 1, 0))
        );
    }


    @Test
    void getWorkingOnTickets() {
        int page = 1;
        int limit = 1;

        List<TicketEntity> ticketEntities = List.of(new TicketEntity());

        PageImpl<TicketEntity> pagedTicketEntities =
                new PageImpl<>(ticketEntities,
                        Pageable.ofSize(page), limit);

        List<TicketDto> ticketDtos = List.of(new TicketDto());

        PageImpl<TicketDto> outputPagedTicketDtos =
                new PageImpl<>(ticketDtos,
                        Pageable.ofSize(page), limit);


        when(userRepository.findByPublicId(anyString()))
                .thenReturn(Optional.of(outputUserEntity));
        when(bugRepo.findAllByAssignedDevsIn(anySet(), any(Pageable.class)))
                .thenReturn(pagedTicketEntities);
        when(modelMapper.map(
                ArgumentMatchers.<Page<TicketEntity>>any(),
                eq(new TypeToken<Page<TicketDto>>() {
                }.getType())))
                .thenReturn(outputPagedTicketDtos);

        Page<TicketDto> workingOnTickets =
                userServiceImp.getWorkingOnTickets(userPublicId, page, limit);


        assertNotNull(workingOnTickets);

        assertEquals(ticketEntities.size(), workingOnTickets.getTotalElements());
        assertEquals(0, workingOnTickets.getNumber());
        assertEquals(limit, workingOnTickets.getSize());
    }

    @Test
    void getWorkingOnTickets_Exceptions() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->
                        userServiceImp.getWorkingOnTickets(null, 1, 1)),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        userServiceImp.getWorkingOnTickets(userPublicId, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        userServiceImp.getWorkingOnTickets(userPublicId, 1, 0))
        );
    }


    @Test
    void getSubscribedOnProjects() {
        int page = 1;
        int limit = 1;

        List<ProjectEntity> projectEntities = List.of(new ProjectEntity());

        PageImpl<ProjectEntity> pagedProjectEntities =
                new PageImpl<>(projectEntities,
                        Pageable.ofSize(page), limit);

        List<ProjectDto> projectDtos = List.of(new ProjectDto());

        PageImpl<ProjectDto> outputPagedProjectDtos =
                new PageImpl<>(projectDtos,
                        Pageable.ofSize(page), limit);

        when(userRepository.findByPublicId(anyString()))
                .thenReturn(Optional.of(outputUserEntity));
        when(projectRepository.findAllBySubscribersIn(anySet(), any(Pageable.class)))
                .thenReturn(pagedProjectEntities);
        when(modelMapper.map(
                ArgumentMatchers.<Page<ProjectEntity>>any(),
                eq(new TypeToken<Page<ProjectDto>>() {
                }.getType())))
                .thenReturn(outputPagedProjectDtos);


        Page<ProjectDto> pagedProjectDtos =
                userServiceImp.getSubscribedOnProjects(userPublicId, page, limit);


        assertNotNull(pagedProjectDtos);

        assertEquals(outputPagedProjectDtos.getTotalElements(), pagedProjectDtos.getTotalElements());
        assertEquals(0, pagedProjectDtos.getNumber());
        assertEquals(limit, pagedProjectDtos.getSize());

    }

    @Test
    void getSubscribedOnProjects_Exceptions() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->
                        userServiceImp.getSubscribedOnProjects(null, 1, 1)),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        userServiceImp.getSubscribedOnProjects(userPublicId, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        userServiceImp.getSubscribedOnProjects(userPublicId, 1, 0))
        );
    }

    @Test
    void loadUserByUsername() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(outputUserEntity));
        when(modelMapper.map(any(UserEntity.class), eq(UserDto.class)))
                .thenReturn(outputUserDto);

        UserDetails userDetails = userServiceImp.loadUserByUsername(userEmail);

        assertNotNull(userDetails);

        assertEquals(outputUserDto.getEmail(), userDetails.getUsername());
    }


}