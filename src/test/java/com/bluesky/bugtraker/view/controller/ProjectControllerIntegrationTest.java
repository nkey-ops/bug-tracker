package com.bluesky.bugtraker.view.controller;

import static io.restassured.RestAssured.form;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bluesky.bugtraker.BugTrackerApplication;
import com.bluesky.bugtraker.TestConfigurations;
import com.bluesky.bugtraker.io.entity.CommentEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.RoleEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
class ProjectControllerIntegrationTest {
  @LocalServerPort private Integer port;

  @Value("${super-admin-user.email}")
  private String email;

  @Value("${super-admin-user.password}")
  private String password;

  @Autowired private UserRepository userRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private CommentRepository commentRepository;

  @Autowired private ModelMapper modelMapper;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private UserEntity userEntity;
  private ProjectEntity projectEntity;

  private HashMap<String, String> dataTablesParams;

  @BeforeEach
  void setUp() {
    initEntities();
    System.out.println();

    RestAssured.authentication =
        form(email, password, new FormAuthConfig("/users/login", "email", "password"));
    RestAssured.port = port;
    RestAssured.basePath = "/users/" + userEntity.getPublicId() + "/projects";
  }

  public void initEntities() {
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

    dataTablesParams = new HashMap<>();
    dataTablesParams.put("draw", "1");
    dataTablesParams.put("start", "0");
    dataTablesParams.put("length", "-1");

    dataTablesParams.put("columns[0].data", "publicId");
    dataTablesParams.put("columns[0].searchable", "true");
    dataTablesParams.put("columns[0].orderable", "true");
    dataTablesParams.put("columns[0].search.regex", "false");
    dataTablesParams.put("columns[0].search.value", "");
    dataTablesParams.put("columns[0].name", "");

    dataTablesParams.put("order[0].column", "0");
    dataTablesParams.put("order[0].dir", "asc");
  }

  @Test
  void createtroject() {
    userEntity = userRepository.save(userEntity);

    ProjectRequestModel projectRequestModel = new ProjectRequestModel();
    projectRequestModel.setName("name");

    Map<String, String> mappedObject =
        objectMapper.convertValue(projectRequestModel, new TypeReference<>() {});

    // @formatter:off
    given()
        .log()
        .all()
        .formParams(mappedObject)
        .when()
        .contentType(ContentType.URLENC)
        .post()
        .then()
        .log()
        .all()
        .assertThat()
        .statusCode(HttpStatus.CREATED.value())
        .extract()
        .response();
    // @formatter:on

    List<ProjectEntity> projects = (List<ProjectEntity>) projectRepository.findAll();

    assertEquals(1, projects.size());
    assertEquals(projectRequestModel.getName(), projects.get(0).getName());
  }

  @Test
  public void getProject() throws JsonProcessingException {
    userEntity = userRepository.save(userEntity);

    projectEntity.setCreator(userEntity);
    projectEntity = projectRepository.save(projectEntity);

    ProjectResponseModel expectedProject =
        modelMapper.map(projectEntity, ProjectResponseModel.class);

    // @formatter:off
    Response response =
        given()
            .log()
            .all()
            .when()
            .contentType(ContentType.URLENC)
            .get("/" + projectEntity.getPublicId())
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response();
    // @formatter:on

    ProjectResponseModel actualProject =
        objectMapper
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .readValue(response.asPrettyString(), ProjectResponseModel.class);

    assertNotNull(actualProject);
    assertEquals(expectedProject, actualProject);
  }

  @Test
  void getProjects() {
    userEntity = userRepository.save(userEntity);

    projectEntity.setCreator(userEntity);
    projectEntity = projectRepository.save(projectEntity);

    ProjectResponseModel expectedProject =
        modelMapper.map(projectEntity, ProjectResponseModel.class);

    // @formatter:off
    Response response =
        given()
            .formParams(dataTablesParams)
            .log()
            .all()
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
    assertEquals(1, result.getRecordsTotal());

    ProjectResponseModel actualProject =
        objectMapper.convertValue(result.getData().get(0), ProjectResponseModel.class);

    assertEquals(expectedProject, actualProject);
  }

  @Test
  void updateProject() {
    userEntity = userRepository.save(userEntity);

    projectEntity.setCreator(userEntity);
    projectEntity = projectRepository.save(projectEntity);

    HashMap<String, String> projectRequestModel = new HashMap<>();
    projectRequestModel.put("name", "new name");

    // @formatter:off
    Response response =
        given()
            .formParams(projectRequestModel)
            .log()
            .all()
            .when()
            .contentType(ContentType.URLENC)
            .patch("/" + projectEntity.getPublicId())
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract()
            .response();
    // @formatter:on

    Optional<ProjectEntity> optionalProject =
        projectRepository.findByPublicId(projectEntity.getPublicId());
    assertTrue(optionalProject.isPresent());

    ProjectEntity actualProject = optionalProject.get();
    assertEquals(projectRequestModel.get("name"), actualProject.getName());
  }

  @Test
  void deleteProject() {
    userEntity = userRepository.save(userEntity);

    projectEntity.setCreator(userEntity);
    projectEntity = projectRepository.save(projectEntity);

    // @formatter:off
    given()
        .log()
        .all()
        .when()
        .contentType(ContentType.URLENC)
        .delete("/" + projectEntity.getPublicId())
        .then()
        .log()
        .all()
        .assertThat()
        .statusCode(HttpStatus.NO_CONTENT.value())
        .extract()
        .response();
    // @formatter:on

    assertFalse(
        projectRepository.existsByPublicId(projectEntity.getPublicId()),
        "Project should not exists");
  }

  @Test
  void addSubscriber() {
    userEntity = userRepository.save(userEntity);

    projectEntity.setCreator(userEntity);
    projectEntity = projectRepository.save(projectEntity);

    // @formatter:off
    given()
        .formParam("publicId", userEntity.getPublicId())
        .log()
        .all()
        .when()
        .contentType(ContentType.URLENC)
        .post("/{projectId}/subscribers", projectEntity.getPublicId())
        .then()
        .log()
        .all()
        .assertThat()
        .statusCode(HttpStatus.CREATED.value())
        .extract()
        .response();
    // @formatter:on

    assertTrue(
        projectRepository.existsByPublicIdAndSubscribersIn(
            projectEntity.getPublicId(), Set.of(userEntity)),
        "User should be added");
  }

  @Test
  void getSubscribers() {
    userEntity = userRepository.save(userEntity);

    projectEntity.setCreator(userEntity);
    projectEntity.addSubscriber(userEntity);
    projectEntity = projectRepository.save(projectEntity);

    UserResponseModel expectedUser = modelMapper.map(userEntity, UserResponseModel.class);

    // @formatter:off
    DataTablesOutput<?> result =
        given()
            .formParams(dataTablesParams)
            .log()
            .all()
            .when()
            .contentType(ContentType.URLENC)
            .get("/{projectId}/subscribers", projectEntity.getPublicId())
            .then()
            .log()
            .all()
            .assertThat()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .response()
            .as(DataTablesOutput.class);
    // @formatter:on

    assertEquals(1, result.getRecordsFiltered());

    UserResponseModel actualUser =
        objectMapper.convertValue(result.getData().get(0), UserResponseModel.class);

    assertEquals(expectedUser, actualUser);
  }

  @Test
  void removeSubscriber() {
    userEntity = userRepository.save(userEntity);

    projectEntity.setCreator(userEntity);
    projectEntity.addSubscriber(userEntity);
    projectEntity = projectRepository.save(projectEntity);

    // @formatter:off
    given()
        .log()
        .all()
        .when()
        .contentType(ContentType.URLENC)
        .delete(
            "/{projectId}/subscribers/{subscriberId}",
            projectEntity.getPublicId(),
            userEntity.getPublicId())
        .then()
        .log()
        .all()
        .assertThat()
        .statusCode(HttpStatus.NO_CONTENT.value())
        .extract()
        .response();
    // @formatter:on

    assertFalse(
        projectRepository.existsByPublicIdAndSubscribersIn(
            projectEntity.getPublicId(), Set.of(userEntity)),
        "User should be removed");
  }

  @Test
  void createComment() {
    userEntity = userRepository.save(userEntity);

    projectEntity.setCreator(userEntity);
    projectEntity = projectRepository.save(projectEntity);

    String expectedContent = "expected content";
    // @formatter:off
    given()
        .formParams("content", expectedContent)
        .log()
        .all()
        .when()
        .contentType(ContentType.URLENC)
        .post("/{projectId}/comments", projectEntity.getPublicId())
        .then()
        .log()
        .all()
        .assertThat()
        .statusCode(HttpStatus.CREATED.value())
        .extract()
        .response();
    // @formatter:on

    List<CommentEntity> allByProject = commentRepository.findAllByProject(projectEntity);

    assertNotNull(allByProject);
    assertEquals(1, allByProject.size());
    assertEquals(expectedContent, allByProject.get(0).getContent());
  }

  @Disabled
  @Test
  void getComments() {}
}
