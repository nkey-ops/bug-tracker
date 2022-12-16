package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.BugTrackerApplication;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.impl.UserServiceImp;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.UserRegisterModel;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.util.*;

import static io.restassured.RestAssured.form;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {BugTrackerApplication.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = {"classpath:application-test.properties"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerIntegrationTest {
    private final static String PATH = "/users";
    @LocalServerPort
    private Integer port;
    @Value("${server.servlet.context-path}")
    private  String contextPath;
    
    @Value("${super-admin-user.email}")
    private String email;
    @Value("${super-admin-user.password}")
    private String password;
        
    @Autowired
    private UserRepository userRepository;
    @Autowired 
    private ProjectRepository projectRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private ModelMapper modelMapper;
    
    private UserEntity userEntity;
    private ProjectEntity projectEntity;
    private TicketEntity ticketEntity;
    
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private HashMap<String, String> dataTablesHashMap;

    @BeforeEach
    public void setUp() {
        initEntities();
        
        RestAssured.authentication =
                form(email, password,
                        new FormAuthConfig("/bugtracker/users/login",
                                "email", "password"));
        RestAssured.port = port;
        RestAssured.basePath = contextPath + PATH;
    }
    void initEntities(){
        RoleEntity role = new RoleEntity(Role.ROLE_USER);
        role.setId(1L);
        
        
        userEntity = new UserEntity();
        userEntity.setPublicId("1");
        userEntity.setEmail("email@sample");
        userEntity.setUsername("Username");
        userEntity.setEncryptedPassword("encrypted pass");
        userEntity.setAvatarURL("avatarURL");
        userEntity.setRoleEntity(role);
        
        projectEntity = new ProjectEntity();
        projectEntity.setPublicId("1");
        projectEntity.setName("project name");

        ticketEntity = new TicketEntity();
        ticketEntity.setPublicId("1");
        ticketEntity.setShortDescription("short description");
        ticketEntity.setStatus(Status.TO_FIX);
        ticketEntity.setSeverity(Severity.CRITICAL);
        ticketEntity.setPriority(Priority.HIGH);
        ticketEntity.setHowToReproduce("how to reproduce");
        ticketEntity.setHowToSolve("Solution is not found");
        ticketEntity.setErroneousProgramBehaviour("behavior");
        ticketEntity.setCreatedTime(new Date());
        ticketEntity.setLastUpdateTime(new Date());


        dataTablesHashMap = new HashMap<>();
        dataTablesHashMap.put("draw", "1");
        dataTablesHashMap.put("start", "0");
        dataTablesHashMap.put("length", "-1");

        dataTablesHashMap.put("columns[0].data", "publicId" );
        dataTablesHashMap.put("columns[0].searchable", "true" );
        dataTablesHashMap.put("columns[0].orderable", "true" );
        dataTablesHashMap.put("columns[0].search.regex", "false" );
        dataTablesHashMap.put("columns[0].search.value", "");
        dataTablesHashMap.put("columns[0].name", "");

        dataTablesHashMap.put("order[0].column", "0");
        dataTablesHashMap.put("order[0].dir", "asc");
    }

    @Disabled
    @Test
    void getCurrentUser() {
    }

    @Test
    void createUser() {
        UserRegisterModel userRegisterModel = new UserRegisterModel();
        userRegisterModel.setEmail("somemail@email.com");
        userRegisterModel.setPassword("password");
        userRegisterModel.setUsername("username");

        Map<String, String> mappedObject =
                objectMapper.convertValue(userRegisterModel, new TypeReference<>() {});
        
        //@formatter:off
        given()
            .formParams(mappedObject)
            .log().all()   
        .when()
            .contentType(ContentType.URLENC)
            .post()
        .then()
            .log().all()
            .assertThat()
            .statusCode(HttpStatus.FOUND.value());
        //@formatter:on

        Optional<UserEntity> byEmail = userRepository.findByEmail(userRegisterModel.getEmail());
        assertTrue(byEmail.isPresent());

        UserEntity actualUser = byEmail.get();
        assertEquals(userRegisterModel.getEmail(), actualUser.getEmail());
        assertEquals(userRegisterModel.getUsername(), actualUser.getUsername());
    }

    
    @Test
    void getUser() {
        UserEntity savedUser = userRepository.save(userEntity);

        //@formatter:off
        Response response =
                given()
                .when()
                    .get("/" + savedUser.getPublicId())
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                .extract().response();
        //@formatter:on
        
        UserResponseModel actualUserResponse = response.as(UserResponseModel.class);
        assertNotNull(actualUserResponse);

        assertEquals(userEntity.getUsername(), actualUserResponse.getUsername());
        assertEquals(userEntity.getPublicId(), actualUserResponse.getPublicId());
        assertEquals(userEntity.getEmail(), actualUserResponse.getEmail());
        assertEquals(userEntity.getAvatarURL(), actualUserResponse.getAvatarURL());
        assertEquals(userEntity.getAddress(), actualUserResponse.getAddress());
        assertEquals(userEntity.getPhoneNumber(), actualUserResponse.getPhoneNumber());
        assertEquals(userEntity.getStatus(), actualUserResponse.getStatus());
    }

    
    @Disabled
    @Test
    void getUserInfo() {
    }

    
    @Test
    void updateUser() {
        UserEntity savedUser = userRepository.save(userEntity);

        UserRequestModel userRequestModel = new UserRequestModel();
        userRequestModel.setUsername("new username");
        userRequestModel.setAvatarURL("https://i.imgur.com/USWcocV.png");
        userRequestModel.setAddress("new address");
        userRequestModel.setPhoneNumber("new number");
        userRequestModel.setStatus("new status");

        Map<String, String> mappedObject = 
                objectMapper.convertValue(userRequestModel, new TypeReference<>() {});

        //@formatter:off
        Response response =        
                given()
                     .log().all()
                     .formParams(mappedObject)
                .when()
                    .contentType(ContentType.URLENC)
                    .patch("/" + savedUser.getPublicId())
                .then()
                        .log().all()
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                    .extract().response();
        //@formatter:on
        
        Optional<UserEntity> optionalUserEntity = userRepository.findById(savedUser.getId());
        assertTrue(optionalUserEntity.isPresent());

        UserEntity actualUserEntity = optionalUserEntity.get();

        assertEquals(userRequestModel.getUsername(), actualUserEntity.getUsername());
        assertEquals(userRequestModel.getAvatarURL(), actualUserEntity.getAvatarURL());
        assertEquals(userRequestModel.getAddress(), actualUserEntity.getAddress());
        assertEquals(userRequestModel.getPhoneNumber(), actualUserEntity.getPhoneNumber());
        assertEquals(userRequestModel.getStatus(), actualUserEntity.getStatus());
    }

    
    @Test
    void getUsers() {
        UserEntity savedUser = userRepository.save(userEntity);
        UserResponseModel expectedUser = modelMapper.map(savedUser, UserResponseModel.class);
        
        //@formatter:off
        Response response =
                given()
                     .log().all() 
                     .formParams(dataTablesHashMap)
                .when()
                    .contentType(ContentType.URLENC)
                    .get()
                .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())  
                .extract().response();
        //@formatter:on


        DataTablesOutput<?> result = response.as(DataTablesOutput.class);
        assertEquals(2 , result.getRecordsTotal());

        
        UserResponseModel actualUser = 
                objectMapper.convertValue(result.getData().get(0), UserResponseModel.class);

        assertEquals(expectedUser, actualUser);
    }

    
    @Test
    void deleteUser() {
        userEntity.setId(null);
        UserEntity savedUser = userRepository.save(userEntity);

        //@formatter:off
        Response response =
            given()
                .log().all()
            .when()
                .delete("/" + savedUser.getPublicId())
            .then()
                .log().all()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().response();
        //@formatter:on

        assertFalse(userRepository.existsById(savedUser.getId()), "User should be deleted");
    }

    
    @Test
    void getSubscribedToProjects() {
        UserEntity savedUser = userRepository.save(userEntity);
        
        projectEntity.setCreator(savedUser);
        projectEntity.addSubscriber(savedUser);
        
        ProjectEntity savedProject = projectRepository.save(projectEntity);
        ProjectResponseModel expectedProject = modelMapper.map(savedProject, ProjectResponseModel.class);

        //@formatter:off
        Response response =
                given()
                    .formParams(dataTablesHashMap)
                    .log().all()
                .when()
                    .contentType(ContentType.URLENC)
                    .get("/" +  savedUser.getPublicId() + "/project-subscriptions")
                .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                .extract().response();
        //@formatter:on

        DataTablesOutput<?> result = response.as(DataTablesOutput.class);
        assertEquals(1 , result.getRecordsTotal());

        ProjectResponseModel actualProject =
                objectMapper.convertValue(result.getData().get(0), ProjectResponseModel.class);

        assertEquals(expectedProject, actualProject);

    }

    
    @Test
    void getSubscribedToTickets() {
        UserEntity savedUser = userRepository.save(userEntity);

        projectEntity.setCreator(savedUser);
        projectEntity.addSubscriber(savedUser);
        ProjectEntity savedProject = projectRepository.save(projectEntity);
        
        ticketEntity.setProject(savedProject);
        ticketEntity.setReporter(savedUser);
        ticketEntity.addSubscriber(savedUser);
        TicketEntity savedTicket = ticketRepository.save(ticketEntity);
        
        TicketResponseModel expectedTicket = modelMapper.map(savedTicket, TicketResponseModel.class);


        //@formatter:off
        Response response =
                given()
                    .formParams(dataTablesHashMap)
                    .log().all()
                .when()
                    .contentType(ContentType.URLENC)
                    .get("/" +  savedUser.getPublicId() + "/ticket-subscriptions")
                .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                .extract().response();
        //@formatter:on


        DataTablesOutput<?> result = response.as(DataTablesOutput.class);
        assertEquals(1 , result.getRecordsTotal());

        TicketResponseModel actualTicket =
                objectMapper.convertValue(result.getData().get(0), TicketResponseModel.class);

        assertEquals(expectedTicket, actualTicket);
    }

    @Disabled
    @Test
    void getTicketsInfo() {
    }
}