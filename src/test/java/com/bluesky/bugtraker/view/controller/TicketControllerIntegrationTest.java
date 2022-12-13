package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.BugTrackerApplication;
import com.bluesky.bugtraker.io.entity.*;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.TicketRecordsRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.model.rensponse.TicketRecordResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static io.restassured.RestAssured.form;
import static io.restassured.RestAssured.given;
import static org.hibernate.validator.internal.util.Contracts.assertNotEmpty;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {BugTrackerApplication.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = {"classpath:application-test.properties"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TicketControllerIntegrationTest {
    @LocalServerPort
    private Integer port;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${super-admin-user.email}")
    private String email;
    @Value("${super-admin-user.password}")
    private String password;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketRecordsRepository ticketRecordsRepository;
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ModelMapper modelMapper;
    
    private UserEntity userEntity;
    private ProjectEntity projectEntity;
    private TicketEntity ticketEntity;
    private TicketRecordEntity ticketRecordEntity;
    private HashMap<String, String> dataTablesParams;


    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        initEntities();

        RestAssured.authentication =
                form(email, password,
                        new FormAuthConfig("/bugtracker/users/login",
                                "email", "password"));
        RestAssured.port = port;
        RestAssured.basePath =  contextPath + 
                "/users/" + userEntity.getPublicId() + 
                "/projects/" + projectEntity.getPublicId() +
                "/tickets/";
    
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
        projectEntity.setCreator(userEntity);

        userRepository.save(userEntity);

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

        ticketRecordEntity = new TicketRecordEntity();
        BeanUtils.copyProperties(ticketEntity, ticketRecordEntity);

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
    void createTicket() throws JsonProcessingException {
        HashMap<String, String> ticketRequestModel = new HashMap<>();
        ticketRequestModel.put("status", ticketEntity.getStatus().getName());
        ticketRequestModel.put("priority", ticketEntity.getPriority().getName());
        ticketRequestModel.put("severity", ticketEntity.getSeverity().getName());
        ticketRequestModel.put("shortDescription", ticketEntity.getShortDescription());
        ticketRequestModel.put("howToReproduce", ticketEntity.getHowToReproduce());
        ticketRequestModel.put("erroneousProgramBehaviour", ticketEntity.getErroneousProgramBehaviour());

        TicketResponseModel expectedTicket = new TicketResponseModel();
        BeanUtils.copyProperties(ticketEntity, expectedTicket);
                
        //@formatter:off
        Response response =
            given()
                .log().all()
                .formParams(ticketRequestModel)
            .when()
                .contentType(ContentType.URLENC)
                .post()
            .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.CREATED.value())
            .extract().response();
        //@formatter:on

        TicketResponseModel actualTicket = 
                objectMapper
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue(response.prettyPrint(), TicketResponseModel.class);

        assertNotNull(actualTicket);
        assertEquals(expectedTicket.getPriority(), actualTicket.getPriority());
        assertEquals(expectedTicket.getStatus(), actualTicket.getStatus());
        assertEquals(expectedTicket.getSeverity(), actualTicket.getSeverity());
        assertEquals(expectedTicket.getShortDescription(), actualTicket.getShortDescription());
        assertEquals(expectedTicket.getErroneousProgramBehaviour(), actualTicket.getErroneousProgramBehaviour());
        assertEquals(expectedTicket.getHowToReproduce(), actualTicket.getHowToReproduce());
    }
    
    @Test
    void getTicket() throws JsonProcessingException {
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity = ticketRepository.save(ticketEntity);

        TicketResponseModel expectedTicket = new TicketResponseModel();
        BeanUtils.copyProperties(ticketEntity, expectedTicket);

        //@formatter:off
        Response response =
                given()
                    .log().all()
                .when()
                    .contentType(ContentType.URLENC)
                    .get("/{ticketId}", ticketEntity.getPublicId())
                .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                .extract().response();
        //@formatter:on

        TicketResponseModel actualTicket =
                objectMapper
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue(response.prettyPrint(), TicketResponseModel.class);

        assertNotNull(actualTicket);
        assertEquals(expectedTicket.getPriority(), actualTicket.getPriority());
        assertEquals(expectedTicket.getStatus(), actualTicket.getStatus());
        assertEquals(expectedTicket.getSeverity(), actualTicket.getSeverity());
        assertEquals(expectedTicket.getShortDescription(), actualTicket.getShortDescription());
        assertEquals(expectedTicket.getErroneousProgramBehaviour(), actualTicket.getErroneousProgramBehaviour());
        assertEquals(expectedTicket.getHowToReproduce(), actualTicket.getHowToReproduce());
    }
    
    @Test
    void getTickets(){
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity = ticketRepository.save(ticketEntity);

        TicketResponseModel expectedTicket = new TicketResponseModel();
        BeanUtils.copyProperties(ticketEntity, expectedTicket);

        //@formatter:off
        DataTablesOutput<?> response =
                given()
                    .formParams(dataTablesParams)
                    .log().all()
                .when()
                    .contentType(ContentType.URLENC)
                    .get()
                .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                .extract()
                        .response()
                        .as(DataTablesOutput.class);
        //@formatter:on

        assertEquals(1, response.getRecordsTotal());

        TicketResponseModel actualTicket =
                objectMapper.convertValue(response.getData().get(0), TicketResponseModel.class);

        assertEquals(expectedTicket, actualTicket);
    }
    
    @Test
    void updateTicket(){
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity = ticketRepository.save(ticketEntity);

        HashMap<String, String> ticketRequest = new HashMap<>();
        ticketRequest.put("status", Status.COMPLETED.getName());
        ticketRequest.put("priority", Priority.LOW.getName());
        ticketRequest.put("severity", Severity.CRITICAL.getName());
        ticketRequest.put("shortDescription", "new short description");
        ticketRequest.put("howToReproduce", "new how to reproduce");
        ticketRequest.put("erroneousProgramBehaviour", "new erroneous program behavior");
        ticketRequest.put("howToSolve", "new solution");

        TicketResponseModel expectedTicket = new TicketResponseModel();
        BeanUtils.copyProperties(ticketEntity, expectedTicket);

        //@formatter:off
        given()
            .formParams(ticketRequest)
            .log().all()
        .when()
            .contentType(ContentType.URLENC)
            .patch("/{ticketId}", ticketEntity.getPublicId())
        .then()
            .log().all()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value())
        .extract().response();
        //@formatter:on

        Optional<TicketEntity> optionalTicket = ticketRepository.findByPublicId(ticketEntity.getPublicId());
        assertTrue(optionalTicket.isPresent());

        TicketEntity actualTicket = optionalTicket.get();
        assertEquals(ticketRequest.get("status"), actualTicket.getStatus().name());
        assertEquals(ticketRequest.get("priority"), actualTicket.getPriority().name());
        assertEquals(ticketRequest.get("severity"), actualTicket.getSeverity().name());
        assertEquals(ticketRequest.get("shortDescription"), actualTicket.getShortDescription());
        assertEquals(ticketRequest.get("howToReproduce"), actualTicket.getHowToReproduce());
        assertEquals(ticketRequest.get("erroneousProgramBehaviour"), actualTicket.getErroneousProgramBehaviour());
        assertEquals(ticketRequest.get("howToSolve"), actualTicket.getHowToSolve());
    }
    
    
    @Test
    void deleteTicket(){
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity = ticketRepository.save(ticketEntity);

        //@formatter:off
        given()
            .log().all()
        .when()
            .contentType(ContentType.URLENC)
            .delete("/{ticketId}", ticketEntity.getPublicId())
        .then()
            .log().all()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());
        //@formatter:on

        assertFalse(ticketRepository.existsByPublicId(ticketEntity.getPublicId()));
    }
    
    @Test
    void getTicketRecords(){
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity = ticketRepository.save(ticketEntity);

        ticketRecordEntity.setMainTicket(ticketEntity);
        ticketRecordEntity.setCreator(userEntity);
        ticketRecordEntity = ticketRecordsRepository.save(ticketRecordEntity);
        
        TicketRecordResponseModel expectedTicketRecord = new TicketRecordResponseModel();
        modelMapper.map(ticketRecordEntity, expectedTicketRecord);

        //@formatter:off
        DataTablesOutput<?> response =
                given()
                    .formParams(dataTablesParams)
                    .log().all()
                .when()
                    .contentType(ContentType.URLENC)
                    .get("/{ticketId}/records", ticketEntity.getPublicId())
                .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                .extract()
                    .response()
                    .as(DataTablesOutput.class);
        //@formatter:on

        assertEquals(1, response.getRecordsTotal());

        TicketRecordResponseModel actualTicketRecord =
                objectMapper.convertValue(response.getData().get(0), TicketRecordResponseModel.class);

        assertEquals(expectedTicketRecord, actualTicketRecord);        
    }

    @Test
    void getTicketRecord() throws JsonProcessingException {
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity = ticketRepository.save(ticketEntity);

        ticketRecordEntity.setMainTicket(ticketEntity);
        ticketRecordEntity.setCreator(userEntity);
        ticketRecordEntity = ticketRecordsRepository.save(ticketRecordEntity);
        
        TicketRecordResponseModel expectedTicketRecord = new TicketRecordResponseModel();
        modelMapper.map(ticketRecordEntity, expectedTicketRecord);

        //@formatter:off
        Response response =
                given()
                    .formParams(dataTablesParams)
                    .log().all()
                .when()
                    .contentType(ContentType.URLENC)
                    .get("/{ticketId}/records/{recordId}", 
                            ticketEntity.getPublicId(), ticketEntity.getPublicId())
                .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                .extract().response();
        //@formatter:on

        TicketRecordResponseModel actualTicketRecord =
                objectMapper
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue(response.prettyPrint(), TicketRecordResponseModel.class);


        assertNotNull(actualTicketRecord);
        assertEquals(expectedTicketRecord, actualTicketRecord);        
    }

    @Test 
    void createComment(){
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity = ticketRepository.save(ticketEntity);
        
        String expectedContent = "expected content";
        //@formatter:off
        given()
            .formParams("content", expectedContent)
            .log().all()
        .when()
            .contentType(ContentType.URLENC)
            .post("/{ticketId}/comments", ticketEntity.getPublicId())
        .then()
            .log().all()
            .assertThat()
            .statusCode(HttpStatus.CREATED.value())
        .extract().response();
        //@formatter:on

        List<CommentEntity> allComments = commentRepository.findAllByTicket(ticketEntity);

        Assertions.assertNotNull(allComments);
        assertEquals(1, allComments.size());
        assertEquals(expectedContent, allComments.get(0).getContent());
    }
    
    @Test
    @Disabled
    void getComments(){}

    @Test
    void addSubscriber() {
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity = ticketRepository.save(ticketEntity);


        //@formatter:off
        given()
            .formParam("publicId", userEntity.getPublicId())
            .log().all()
        .when()
            .contentType(ContentType.URLENC)
            .post("/{ticketId}/subscribers", ticketEntity.getPublicId() )
        .then()
            .log().all()
            .assertThat()
            .statusCode(HttpStatus.CREATED.value())
        .extract().response();
        //@formatter:on

        assertTrue(
                ticketRepository.existsByPublicIdAndSubscribersIn(
                        ticketEntity.getPublicId(), Set.of(userEntity)),
                "User should be added");
    }

    @Test
    void getSubscribers() {
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity.addSubscriber(userEntity);
        ticketEntity = ticketRepository.save(ticketEntity);


        UserResponseModel expectedUser =
                modelMapper.map(userEntity, UserResponseModel.class);

        //@formatter:off
        DataTablesOutput<?> result =
                given()
                    .formParams(dataTablesParams)
                    .log().all()
                .when()
                    .contentType(ContentType.URLENC)
                    .get("/{ticketId}/subscribers", ticketEntity.getPublicId())
                .then()
                    .log().all()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                .extract()
                    .response()
                    .as(DataTablesOutput.class);
        //@formatter:on

        assertEquals(1, result.getRecordsFiltered());

        UserResponseModel actualUser =
                objectMapper.convertValue(result.getData().get(0), UserResponseModel.class);

        assertEquals(expectedUser, actualUser);

    }

    @Test
    void removeSubscriber() {
        ticketEntity.setReporter(userEntity);
        ticketEntity.setProject(projectEntity);
        ticketEntity.addSubscriber(userEntity);
        ticketEntity = ticketRepository.save(ticketEntity);
        
        //@formatter:off
        given()
                .log().all()
                .when()
                .contentType(ContentType.URLENC)
                .delete("/{ticketId}/subscribers/{subscriberId}",
                        ticketEntity.getPublicId(), userEntity.getPublicId())
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .extract().response();
        //@formatter:on

        assertFalse(
                ticketRepository.existsByPublicIdAndSubscribersIn(
                        projectEntity.getPublicId(), Set.of(userEntity)),
                "User should be removed");
    }
}
