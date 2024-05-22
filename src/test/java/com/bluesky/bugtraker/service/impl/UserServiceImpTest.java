package com.bluesky.bugtraker.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.annotation.Description;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.security.core.userdetails.UserDetails;

import com.bluesky.bugtraker.exceptions.serviceexception.RoleServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.UserServiceException;
import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.RoleRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.utils.DataExtractionUtils;
import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.dto.UserDTO;

class UserServiceImpTest {
    private AutoCloseable closeable;

    @InjectMocks
    private UserServiceImp userService;

    @Mock
    private DataExtractionUtils dataExtractionUtils;

    @Mock
    private UserRepository userRepo;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TicketRepository ticketRepo;
    @Mock
    private ProjectRepository projectRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private Utils utils;
    @Mock
    private ModelMapper modelMapper;

    private UserEntity userEntity;
    private UserDTO userDTO;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        userDTO = new UserDTO();
        userDTO.setPublicId("1");
        userDTO.setEmail("email@sample");
        userDTO.setUsername("Username");
        userDTO.setPassword("password");
        userDTO.setAvatarURL("avatarURL");
        userDTO.setRole(Role.ROLE_USER);

        userEntity = new UserEntity();
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPublicId(userDTO.getPublicId());
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setId(1L);
        userEntity.setPublicId(userDTO.getPublicId());
        userEntity.setEncryptedPassword("encryptedPassword");

        RoleEntity role = new RoleEntity(userDTO.getRole());
        role.setId(1L);
        userEntity.setRoleEntity(role);


        DataTablesInput dataTablesInput = new DataTablesInput();
        dataTablesInput.setLength(1);
        dataTablesInput.setStart(1);
    }

    @AfterEach
    public final void releaseMocks() throws Exception {
        closeable.close();
    }

    @Test
    void isUserExistsByEmail() {
        when(userRepo.existsByEmail(anyString())).thenReturn(true);
        assertTrue(userService.isUserExistsByEmail(userDTO.getEmail()));


        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        assertFalse(userService.isUserExistsByEmail(userDTO.getEmail()));
    }


    @Test
    void getUserById() {
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(userEntity);
        when(modelMapper.map(any(UserEntity.class), eq(UserDTO.class))).thenReturn(userDTO);

        UserDTO actualUserDTO = userService.getUserById(userEntity.getPublicId());

        verify(dataExtractionUtils).getUserEntity(anyString());

        assertNotNull(actualUserDTO);
        assertEquals(userDTO, actualUserDTO);
    }

    @Test
    void getUserById_NullPointerException() {
        assertThrows(NullPointerException.class,
                () -> userService.getUserById(null));
    }

    @Test
    void getUserByEmail() {
        when(userRepo.findByEmail(anyString()))
                .thenReturn(Optional.of(userEntity));
        when(modelMapper.map(any(UserEntity.class), eq(UserDTO.class)))
                .thenReturn(userDTO);

        UserDTO actualUserDTO = userService.getUserByEmail(userDTO.getEmail());

        assertNotNull(actualUserDTO);
        assertEquals(userDTO, actualUserDTO);
    }

    @Test
    void getUserByEmail_UserServiceException() {
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserServiceException.class,
                () -> userService.getUserByEmail(userDTO.getEmail()));
    }

    @Test
    void getUsers() {
        DataTablesInput dataTablesInput = new DataTablesInput();
        dataTablesInput.setLength(1);
        dataTablesInput.setStart(1);

        DataTablesOutput<UserEntity> dataUserEntity = new DataTablesOutput<>();
        dataUserEntity.setRecordsTotal(1);
        dataUserEntity.setData(List.of(userEntity));

        DataTablesOutput<UserDTO> dataUserDTO = new DataTablesOutput<>();
        dataUserEntity.setRecordsTotal(1);
        dataUserDTO.setData(List.of(userDTO));

        when(userRepo.findAll(any(DataTablesInput.class))).thenReturn(dataUserEntity);
        when(utils.map(
                (DataTablesOutput<?>) any(DataTablesOutput.class),
                (TypeToken<List<UserDTO>>) any(TypeToken.class))).thenReturn(dataUserDTO);


        DataTablesOutput<UserDTO> actualDataTablesOutput = userService.getUsers(dataTablesInput);
        verify(userRepo).findAll(any(DataTablesInput.class));
        verify(utils).map(
                (DataTablesOutput<?>) any(DataTablesOutput.class),
                (TypeToken<List<UserDTO>>) any(TypeToken.class));

        assertNotNull(actualDataTablesOutput);
        assertEquals(dataUserDTO, actualDataTablesOutput);

    }
 
    @Test
    @Description("Verifies a user and its email")
    void createUserWithRoles() {
    	String verificationToken = "Mock verification token";

    	UserDTO expected = new ModelMapper().map(userDTO, UserDTO.class);
    	
        when(userRepo.existsByEmail(anyString())).thenReturn(false);

        when(utils.generateUserId()).thenReturn(expected.getPublicId());
        when(utils.encode(anyString())).thenReturn(userEntity.getEncryptedPassword());

        when(modelMapper.map(any(UserDTO.class), eq(UserEntity.class)))
        		.thenReturn(userEntity);
        when(roleRepository.findByRole(any(Role.class)))
        		.thenReturn(Optional.of(userEntity.getRoleEntity()));
        when(utils.getEmailVerificationToken(anyString()))
        		.thenReturn(verificationToken);

        when(userRepo.save(any(UserEntity.class)))
        		.thenReturn(userEntity);
        when(modelMapper.map(any(UserEntity.class), eq(UserDTO.class)))
        		.thenReturn(expected);

        UserDTO actualUserDTO = userService.createUserWithRole(userDTO);

        verify(userRepo).existsByEmail(anyString());
        verify(utils).generateUserId();
        verify(utils).encode(anyString());
        verify(modelMapper).map(any(UserDTO.class), eq(UserEntity.class));
        verify(roleRepository).findByRole(any(Role.class));
        verify(userRepo).save(any(UserEntity.class));
        verify(modelMapper).map(any(UserEntity.class), eq(UserDTO.class));
        verify(emailService).verifyEmail(expected.getEmail(), verificationToken);

        assertNotNull(actualUserDTO);
        assertEquals(expected, actualUserDTO);
    }

    @Test
    void createUserWithRoles_UserServiceException() {
        when(userRepo.existsByEmail(anyString()))
                .thenReturn(true);
        when(modelMapper.map(any(UserDTO.class), eq(UserEntity.class)))
                .thenReturn(userEntity);

        assertThrows(UserServiceException.class,
                () -> userService.createUserWithRole(userDTO));
    }

    @Test
    void createUserWithRoles_RoleServiceException() {
        when(userRepo.existsByEmail(anyString()))
                .thenReturn(false);
        when(modelMapper.map(any(UserDTO.class), eq(UserEntity.class)))
                .thenReturn(userEntity);
        when(roleRepository.findByRole(any(Role.class)))
                .thenReturn(Optional.empty());

        assertThrows(RoleServiceException.class, () -> {
            userService.createUserWithRole(userDTO);
        });
    }

    @Test
    void createUserWithRoles_NullPointerException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->
                        userService.createUserWithRole(null))
        );
    }

    @Test
    void createUser() {
        UserServiceImp spy = spy(userService);

        Mockito.doReturn(userDTO)
                .when(spy)
                .createUserWithRole(any(UserDTO.class));

        UserDTO actualUserDTO =
                spy.createUser(userDTO);

        assertNotNull(actualUserDTO);
        assertEquals(userDTO, actualUserDTO);

    }

    @Test
    void updateUserWhenNoRoleIsSet() {
        userDTO.setRole(null);

        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(userEntity);
        when(userRepo.save(any(UserEntity.class))).thenReturn(userEntity);

        userService.updateUser(userDTO.getPublicId(), userDTO);

        verify(dataExtractionUtils).getUserEntity(anyString());
        verify(userRepo).save(any(UserEntity.class));
    }

    @Test
    void updateUserWhenRoleIsSet() {
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(userEntity);
        when(dataExtractionUtils.getRoleEntityToBeSet(any(Role.class), any(Role.class))).thenReturn(userEntity.getRoleEntity());
        when(userRepo.save(any(UserEntity.class))).thenReturn(userEntity);

        userService.updateUser(userDTO.getPublicId(), userDTO);

        verify(dataExtractionUtils).getUserEntity(anyString());
        verify(dataExtractionUtils).getRoleEntityToBeSet(any(Role.class), any(Role.class));
        verify(userRepo).save(any(UserEntity.class));
    }


    @Test
    void deleteUser() {
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(userEntity);

        userService.deleteUser(userDTO.getPublicId());

        verify(dataExtractionUtils).getUserEntity(anyString());
        verify(userRepo).delete(any(UserEntity.class));
    }


    @Test
    void isSubscribedToProject() {
        when(projectRepo.existsByPublicId(anyString())).thenReturn(true);
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(userEntity);
        when(projectRepo.existsByPublicIdAndSubscribersIn(anyString(), anySet())).thenReturn(true);


        boolean isSubscribedToTicket = userService.isSubscribedToProject(userDTO.getPublicId(), "projectId");

        assertTrue(isSubscribedToTicket);
    }

    @Test
    void isSubscribedToTicket() {
        when(ticketRepo.existsByPublicId(anyString())).thenReturn(true);
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(userEntity);
        when(ticketRepo.existsByPublicIdAndSubscribersIn(anyString(), anySet())).thenReturn(true);

        boolean isSubscribedToTicket = userService.isSubscribedToTicket(userDTO.getPublicId(), "ticketId");

        assertTrue(isSubscribedToTicket);
    }


    @Test
    void loadUserByUsername() {
        userDTO.setPassword(userEntity.getEncryptedPassword());

        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(modelMapper.map(any(UserEntity.class), eq(UserDTO.class))).thenReturn(userDTO);

        UserDetails actualUserDetails = userService.loadUserByUsername(userDTO.getEmail());

        assertNotNull(actualUserDetails);

        assertEquals(userDTO.getEmail(), actualUserDetails.getUsername());
        assertEquals(userEntity.getEncryptedPassword(), actualUserDetails.getPassword());
    }

    
    @Test
    void verifyEmailToken() {
    	UserEntity expected = new ModelMapper().map(userEntity, UserEntity.class); 
    	expected.setEmailVerificationStatus(true);
    	expected.setEmailVerificationToken(null);
    	
    	userEntity.setEmailVerificationStatus(false);
    	userEntity.setEmailVerificationToken("Mock Token");;

    	when(userRepo.findByEmailVerificationToken(anyString()))
    			.thenReturn(Optional.of(userEntity));
    	when(utils.hasEmailTokenExpired(anyString())).thenReturn(false);
    	when(userRepo.save(any())).thenReturn(null);   	
    	
    	userService.verifyEmailToken("Mock Token");
    	
    	verify(userRepo).save(expected);
    }
    
    @Test
    void verifyEmailTokenThrowsException() {
    	when(userRepo.findByEmailVerificationToken(anyString())).thenReturn(Optional.empty());
    	
    	assertThrows(UserServiceException.class, () -> 
    		userService.verifyEmailToken("Mock Token"));
    }

    @Test
    void verifyEmailTokenThrowsExceptionOnExpiredToken() { 
    	when(userRepo.findByEmailVerificationToken(anyString()))
    			.thenReturn(Optional.of(userEntity));
    	when(utils.hasEmailTokenExpired(anyString()))
    			.thenReturn(true);
    	
    	assertThrows(UserServiceException.class, () -> 
    			userService.verifyEmailToken("Mock Token"));
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    


}
