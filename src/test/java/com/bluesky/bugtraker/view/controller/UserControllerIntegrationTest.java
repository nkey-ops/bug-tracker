package com.bluesky.bugtraker.view.controller;

import static io.restassured.RestAssured.form;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bluesky.bugtraker.BugTrackerApplication;
import com.bluesky.bugtraker.TestConfigurations;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.UserRegisterModel;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    classes = {BugTrackerApplication.class, TestConfigurations.class},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIntegrationTest {
  @LocalServerPort private Integer port;
  private static final String PATH = "/users";

  @Value("${super-admin-user.email}")
  private String email;

  @Value("${super-admin-user.password}")
  private String password;

  @Autowired private UserRepository userRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private TicketRepository ticketRepository;
  @Autowired private ModelMapper modelMapper;

  private UserEntity userEntity;
  private ProjectEntity projectEntity;
  private TicketEntity ticketEntity;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private HashMap<String, String> dataTablesHashMap;

  @Value("${user-avatar-url}")
  private String userDefaultAvatarUrl;

  @BeforeEach
  public void setUp() {
    initEntities();

    RestAssured.authentication =
        form(email, password, new FormAuthConfig("/users/login", "email", "password"));
    RestAssured.port = port;
    RestAssured.basePath = PATH;
  }

  void initEntities() {
    RoleEntity role = new RoleEntity(Role.ROLE_USER);
    role.setId(1L);

    userEntity = new UserEntity();
    userEntity.setPublicId("1");
    userEntity.setUsername("Username");
    userEntity.setEmail("email@sample");
    userEntity.setEncryptedPassword("encrypted pass");

    userEntity.setEmailVerificationToken(null);
    userEntity.setEmailVerificationStatus(true);
    userEntity.setAvatarURL("https://i.imgur.com/new-avatar.png");
    userEntity.setAddress("address");
    userEntity.setPhoneNumber("12345");
    userEntity.setStatus("status");
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

    dataTablesHashMap.put("columns[0].data", "publicId");
    dataTablesHashMap.put("columns[0].searchable", "true");
    dataTablesHashMap.put("columns[0].orderable", "true");
    dataTablesHashMap.put("columns[0].search.regex", "false");
    dataTablesHashMap.put("columns[0].search.value", "");
    dataTablesHashMap.put("columns[0].name", "");

    dataTablesHashMap.put("order[0].column", "0");
    dataTablesHashMap.put("order[0].dir", "asc");
  }

  @Disabled
  @Test
  void getCurrentUser() {}

  @Test
  void createUser() {
    UserRegisterModel userRegisterModel = new UserRegisterModel();
    userRegisterModel.setEmail("somemail@email.com");
    userRegisterModel.setPassword("password");
    userRegisterModel.setUsername("username");

    Map<String, String> mappedObject =
        objectMapper.convertValue(userRegisterModel, new TypeReference<>() {});

    // @formatter:off
    given()
        .formParams(mappedObject)
        .log()
        .all()
        .when()
        .contentType(ContentType.URLENC)
        .post()
        .then()
        .log()
        .all()
        .assertThat()
        .statusCode(HttpStatus.FOUND.value());
    // @formatter:on

    Optional<UserEntity> byEmail = userRepository.findByEmail(userRegisterModel.getEmail());
    assertTrue(byEmail.isPresent());

    UserEntity actualUser = byEmail.get();
    assertEquals(userRegisterModel.getEmail(), actualUser.getEmail());
    assertEquals(userRegisterModel.getUsername(), actualUser.getUsername());
  }

  @Test
  void getUser() throws JsonProcessingException {
    UserEntity savedUser = userRepository.save(userEntity);

    // @formatter:off
    Response response =
        given()
            .when()
            .get("/" + savedUser.getPublicId())
            .then()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();
    // @formatter:on

    UserResponseModel actualUser =
        objectMapper
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(response.asPrettyString(), UserResponseModel.class);

    assertNotNull(actualUser);

    assertEquals(userEntity.getUsername(), actualUser.getUsername());
    assertEquals(userEntity.getPublicId(), actualUser.getPublicId());
    assertEquals(userEntity.getEmail(), actualUser.getEmail());
    assertEquals(userEntity.getAvatarURL(), actualUser.getAvatarURL());
    assertEquals(userEntity.getAddress(), actualUser.getAddress());
    assertEquals(userEntity.getPhoneNumber(), actualUser.getPhoneNumber());
    assertEquals(userEntity.getStatus(), actualUser.getStatus());
  }

  @Disabled
  @Test
  void getUserInfo() {}

  @Test
  void updateUser() {
    var userPublicId = userRepository.save(userEntity).getPublicId();

    UserRequestModel userRequestModel = new UserRequestModel();
    userRequestModel.setUsername("new username");
    userRequestModel.setAvatarUrl("https://i.imgur.com/avatar-url.png");
    userRequestModel.setAddress("new address");
    userRequestModel.setPhoneNumber("new number");
    userRequestModel.setStatus("new status");

    Map<String, String> mappedObject =
        objectMapper.convertValue(userRequestModel, new TypeReference<>() {});

    // @formatter:off
    Response response =
        given()
            .log()
            .all()
            .formParams(mappedObject)
            .when()
            .contentType(ContentType.URLENC)
            .patch("/" + userPublicId)
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();
    // @formatter:on

    assertEquals(HttpStatus.OK.value(), response.getStatusCode());

    Optional<UserEntity> optionalUserEntity = userRepository.findByPublicId(userPublicId);
    assertTrue(optionalUserEntity.isPresent());

    UserEntity actualUserEntity = optionalUserEntity.get();
    assertEquals(userRequestModel.getUsername(), actualUserEntity.getUsername());
    assertEquals(userRequestModel.getAvatarUrl(), actualUserEntity.getAvatarURL());
    assertEquals(userRequestModel.getAddress(), actualUserEntity.getAddress());
    assertEquals(userRequestModel.getPhoneNumber(), actualUserEntity.getPhoneNumber());
    assertEquals(userRequestModel.getStatus(), actualUserEntity.getStatus());
    assertEquals(userEntity.getEmail(), actualUserEntity.getEmail());
    assertEquals(userEntity.getEncryptedPassword(), actualUserEntity.getEncryptedPassword());
    assertEquals(
        userEntity.getEmailVerificationToken(), actualUserEntity.getEmailVerificationToken());
    assertEquals(
        userEntity.isEmailVerificationStatus(), actualUserEntity.isEmailVerificationStatus());
  }

  @Test
  void updateUserPartially() {
    var userPublicId = userRepository.save(userEntity).getPublicId();

    UserRequestModel userRequestModel = new UserRequestModel();
    userRequestModel.setUsername("new username");
    userRequestModel.setAvatarUrl(userEntity.getAvatarURL());
    userRequestModel.setAddress(userEntity.getAddress());
    userRequestModel.setPhoneNumber(userEntity.getPhoneNumber());
    userRequestModel.setStatus(userEntity.getStatus());

    Map<String, String> mappedObject =
        objectMapper.convertValue(userRequestModel, new TypeReference<>() {});

    // @formatter:off
    Response response =
        given()
            .log()
            .all()
            .formParams(mappedObject)
            .when()
            .contentType(ContentType.URLENC)
            .patch("/" + userPublicId)
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();
    // @formatter:on

    assertEquals(HttpStatus.OK.value(), response.getStatusCode());

    Optional<UserEntity> optionalUserEntity = userRepository.findByPublicId(userPublicId);
    UserEntity actualUserEntity = optionalUserEntity.get();
    assertTrue(optionalUserEntity.isPresent());

    assertEquals(userRequestModel.getUsername(), actualUserEntity.getUsername());
    assertEquals(userEntity.getAvatarURL(), actualUserEntity.getAvatarURL());
    assertEquals(userEntity.getAddress(), actualUserEntity.getAddress());
    assertEquals(userEntity.getPhoneNumber(), actualUserEntity.getPhoneNumber());
    assertEquals(userEntity.getStatus(), actualUserEntity.getStatus());
    assertEquals(userEntity.getEmail(), actualUserEntity.getEmail());
    assertEquals(userEntity.getEncryptedPassword(), actualUserEntity.getEncryptedPassword());
    assertEquals(
        userEntity.getEmailVerificationToken(), actualUserEntity.getEmailVerificationToken());
    assertEquals(
        userEntity.isEmailVerificationStatus(), actualUserEntity.isEmailVerificationStatus());
  }

  @Test
  void getUsers() {
    UserEntity savedUser = userRepository.save(userEntity);
    UserResponseModel expectedUser = modelMapper.map(savedUser, UserResponseModel.class);

    // @formatter:off
    Response response =
        given()
            .log()
            .all()
            .formParams(dataTablesHashMap)
            .when()
            .contentType(ContentType.URLENC)
            .get()
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();
    // @formatter:on

    DataTablesOutput<?> result = response.as(DataTablesOutput.class);
    assertEquals(2, result.getRecordsTotal());

    UserResponseModel actualUser =
        objectMapper.convertValue(result.getData().get(0), UserResponseModel.class);

    assertEquals(expectedUser, actualUser);
  }

  @Test
  void deleteUser() {
    userEntity.setId(null);
    UserEntity savedUser = userRepository.save(userEntity);

    // @formatter:off
    Response response =
        given()
            .log()
            .all()
            .when()
            .delete("/" + savedUser.getPublicId())
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();
    // @formatter:on

    assertFalse(userRepository.existsById(savedUser.getId()), "User should be deleted");
  }

  @Test
  void getSubscribedToProjects() {
    UserEntity savedUser = userRepository.save(userEntity);

    projectEntity.setCreator(savedUser);
    projectEntity.addSubscriber(savedUser);

    ProjectEntity savedProject = projectRepository.save(projectEntity);
    ProjectResponseModel expectedProject =
        modelMapper.map(savedProject, ProjectResponseModel.class);

    // @formatter:off
    Response response =
        given()
            .formParams(dataTablesHashMap)
            .log()
            .all()
            .when()
            .contentType(ContentType.URLENC)
            .get("/" + savedUser.getPublicId() + "/project-subscriptions")
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();
    // @formatter:on

    DataTablesOutput<?> result = response.as(DataTablesOutput.class);
    assertEquals(1, result.getRecordsTotal());

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

    // @formatter:off
    Response response =
        given()
            .formParams(dataTablesHashMap)
            .log()
            .all()
            .when()
            .contentType(ContentType.URLENC)
            .get("/" + savedUser.getPublicId() + "/ticket-subscriptions")
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();
    // @formatter:on

    DataTablesOutput<?> result = response.as(DataTablesOutput.class);
    assertEquals(1, result.getRecordsTotal());

    TicketResponseModel actualTicket =
        objectMapper.convertValue(result.getData().get(0), TicketResponseModel.class);

    assertEquals(expectedTicket, actualTicket);
  }

  @Disabled
  @Test
  void getTicketsInfo() {}
}
