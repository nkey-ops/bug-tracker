package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.io.entity.*;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.TicketRecordsRepository;
import com.bluesky.bugtraker.io.repository.TicketRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.service.utils.ServiceUtils;
import com.bluesky.bugtraker.shared.dto.*;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;

import static com.bluesky.bugtraker.service.TestUtils.assertEqualsTicket;
import static com.bluesky.bugtraker.service.TestUtils.assertEqualsTicketRecord;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImpTest {

    @InjectMocks
    private TicketServiceImp ticketService;
    private TicketServiceImp ticketServiceSpy;
    @Mock
    private TicketRepository ticketRepo;
    @Mock
    private TicketRecordsRepository ticketRecordRepo;
    @Mock
    private CommentRepository commentRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private ServiceUtils serviceUtils;
    @Mock
    private Utils utils;
    @Mock
    private ModelMapper modelMapper;
    private TicketEntity ticketEntity;
    private TicketDTO ticketDTO;
    private ProjectDTO projectDTO;
    private ProjectEntity projectEntity;
    private UserDTO creatorDTO;
    private UserEntity creatorEntity;
    private DataTablesInput dataTablesInput;


    @BeforeEach
    void setUp() {
        ticketServiceSpy = Mockito.spy(ticketService);

        creatorDTO = new UserDTO();
        creatorDTO.setPublicId("1");
        creatorDTO.setEmail("email@sample");
        creatorDTO.setUsername("Username");
        creatorDTO.setPassword("password");
        creatorDTO.setAvatarURL("avatarURL");

        creatorEntity = new UserEntity();
        creatorEntity.setEmail(creatorDTO.getEmail());
        creatorEntity.setPublicId(creatorDTO.getPublicId());
        creatorEntity.setUsername(creatorDTO.getUsername());
        creatorEntity.setId(1L);
        creatorEntity.setPublicId(creatorDTO.getPublicId());
        creatorEntity.setEncryptedPassword("encryptedPassword");

        projectDTO = new ProjectDTO();
        projectDTO.setPublicId("1");
        projectDTO.setName("projectName");
        projectDTO.setCreator(creatorDTO);

        projectEntity = new ProjectEntity();
        projectEntity.setId(1L);
        projectEntity.setPublicId(projectDTO.getPublicId());
        projectEntity.setName(projectDTO.getName());
        projectEntity.setCreator(creatorEntity);

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

        dataTablesInput = new DataTablesInput();
        dataTablesInput.setStart(1);
        dataTablesInput.setLength(1);
        dataTablesInput.setDraw(1);
    }
    

    @Test
    void getTicket() {
        when(serviceUtils.getTicketEntity(anyString())).thenReturn(ticketEntity);
        when(modelMapper.map(any(TicketEntity.class), eq(TicketDTO.class))).thenReturn(ticketDTO);

        TicketDTO actualTicketDTO = ticketService.getTicket(ticketEntity.getPublicId());
        assertEquals(ticketDTO, actualTicketDTO);
    }

    @Test
    void getTickets() {
        DataTablesOutput<TicketEntity> dataTablesOutputEntity = new DataTablesOutput<>();
        dataTablesOutputEntity.setDraw(dataTablesInput.getDraw());
        dataTablesOutputEntity.setData(List.of(ticketEntity));

        DataTablesOutput<TicketDTO> dataTablesOutputDTO = new DataTablesOutput<>();
        dataTablesOutputDTO.setDraw(dataTablesInput.getDraw());
        dataTablesOutputDTO.setData(List.of(ticketDTO));

        when(serviceUtils.getProjectEntity(anyString())).thenReturn(projectEntity);
        when(ticketRepo.findAll(any(DataTablesInput.class),
                ArgumentMatchers.<Specification<TicketEntity>>any())).thenReturn(dataTablesOutputEntity);
        when(utils.map(ArgumentMatchers.<DataTablesOutput<TicketEntity>>any(),
                ArgumentMatchers.<TypeToken<List<TicketDTO>>>any())).thenReturn(dataTablesOutputDTO);


        DataTablesOutput<TicketDTO> actualDataTablesOutputDTO =
                ticketService.getTickets(projectEntity.getPublicId(), dataTablesInput);


        verify(ticketRepo).findAll(eq(dataTablesInput), ArgumentMatchers.<Specification<TicketEntity>>any());
        verify(utils).map(eq(dataTablesOutputEntity),
                ArgumentMatchers.<TypeToken<List<TicketDTO>>>any());

        assertNotNull(actualDataTablesOutputDTO);
        assertEquals(dataTablesOutputDTO, actualDataTablesOutputDTO);
    }

    @Test
    void createTicket() {
        TicketEntity inputTicketEntity = new TicketEntity();
        new ModelMapper().map(ticketEntity, inputTicketEntity);

        inputTicketEntity.setPublicId(null);
        inputTicketEntity.setId(null);
        inputTicketEntity.setCreatedTime(null);
        inputTicketEntity.setLastUpdateTime(null);
        inputTicketEntity.setHowToSolve(null);

        ticketDTO.setPublicId(null);
        ticketEntity.setId(null);

        ArgumentCaptor<TicketEntity> ticketCaptor = ArgumentCaptor.forClass(TicketEntity.class);

        when(serviceUtils.getProjectEntity(anyString())).thenReturn(projectEntity);
        when(modelMapper.map(any(TicketDTO.class), eq(TicketEntity.class))).thenReturn(inputTicketEntity);
        when(utils.generateRandomString(anyInt())).thenReturn(projectEntity.getPublicId());
        when(serviceUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(ticketRepo.save(any(TicketEntity.class))).thenReturn(ticketEntity);
        doNothing().when(ticketServiceSpy)
                .createTicketRecord(projectEntity.getPublicId(), creatorEntity.getPublicId());

        ticketServiceSpy.createTicket(projectEntity.getPublicId(), ticketDTO, creatorEntity.getPublicId());

        verify(ticketRepo).save(ticketCaptor.capture());
        verify(ticketServiceSpy).createTicketRecord(projectEntity.getPublicId(), creatorEntity.getPublicId());

        TicketEntity actualTicketEntity = ticketCaptor.getValue();

        assertEqualsTicket(ticketEntity, actualTicketEntity);
    }
    
    @Test
    void updateTicket() {
        TicketDTO ticketDTOUpdate = new TicketDTO();
        ticketDTOUpdate.setShortDescription("new short description");
        ticketDTOUpdate.setStatus(Status.IN_PROGRESS);
        ticketDTOUpdate.setSeverity(Severity.MAJOR);
        ticketDTOUpdate.setPriority(Priority.LOW);
        ticketDTOUpdate.setHowToReproduce("new how to reproduce");
        ticketDTOUpdate.setHowToSolve("new how to solve");

        TicketEntity inputTicketEntity = new TicketEntity();
        new ModelMapper().map(ticketEntity, inputTicketEntity);


        TicketEntity expectedTicketEntity = new TicketEntity();
        new ModelMapper().map(ticketEntity, expectedTicketEntity);
        expectedTicketEntity.setShortDescription(ticketDTOUpdate.getShortDescription());
        expectedTicketEntity.setStatus(ticketDTOUpdate.getStatus());
        expectedTicketEntity.setSeverity(ticketDTOUpdate.getSeverity());
        expectedTicketEntity.setPriority(ticketDTOUpdate.getPriority());
        expectedTicketEntity.setHowToReproduce(ticketDTOUpdate.getHowToReproduce());
        expectedTicketEntity.setHowToSolve(ticketDTOUpdate.getHowToSolve());

        ArgumentCaptor<TicketEntity> ticketEntityCaptor = ArgumentCaptor.forClass(TicketEntity.class);

        when(serviceUtils.getTicketEntity(anyString())).thenReturn(ticketEntity);
        doAnswer(invocation -> {
                    assertEquals(ticketDTOUpdate, invocation.getArgument(0));
                    assertEqualsTicket(ticketEntity, invocation.getArgument(1));
                    TicketEntity ticketEntity = invocation.getArgument(1);
            
                    ticketEntity.setShortDescription(ticketDTOUpdate.getShortDescription());
                    ticketEntity.setStatus(ticketDTOUpdate.getStatus());
                    ticketEntity.setSeverity(ticketDTOUpdate.getSeverity());
                    ticketEntity.setPriority(ticketDTOUpdate.getPriority());
                    ticketEntity.setHowToReproduce(ticketDTOUpdate.getHowToReproduce());
                    ticketEntity.setHowToSolve(ticketDTOUpdate.getHowToSolve());
                    return null;
                }
        ).when(modelMapper).map(any(TicketDTO.class), any(TicketEntity.class));
        doNothing().when(ticketServiceSpy).createTicketRecord(anyString(), anyString());
        when(ticketRepo.save(any(TicketEntity.class))).thenReturn(ticketEntity);

        ticketServiceSpy.updateTicket(ticketEntity.getPublicId(), ticketDTOUpdate, creatorEntity.getPublicId());

        verify(ticketRepo).save(ticketEntityCaptor.capture());
        verify(ticketServiceSpy).createTicketRecord(ticketEntity.getPublicId(), creatorEntity.getPublicId());

        TicketEntity actualTicketEntity = ticketEntityCaptor.getValue();
        assertEqualsTicket(expectedTicketEntity, actualTicketEntity);
    }

    @Test
    void deleteTicket() {
        TicketEntity expectedTicketEntity = new TicketEntity();
        new ModelMapper().map(ticketEntity, expectedTicketEntity);

        projectEntity.addTicket(ticketEntity);

        ArgumentCaptor<TicketEntity> ticketCaptor = ArgumentCaptor.forClass(TicketEntity.class);
        when(serviceUtils.getTicketEntity(anyString())).thenReturn(ticketEntity);

        ticketService.deleteTicket(ticketEntity.getPublicId());

        verify(ticketRepo).delete(ticketCaptor.capture());

        TicketEntity actualTicketEntity = ticketCaptor.getValue();
        assertNotNull(actualTicketEntity);
        assertNull(actualTicketEntity.getProject());
        assertEquals(expectedTicketEntity, actualTicketEntity);
    }

    @Test
    void createTicketRecord() {
        TicketRecordEntity inputTicketRecord = new TicketRecordEntity();
        new ModelMapper().map(ticketEntity, inputTicketRecord);

        TicketRecordEntity expectedTicketRecord = new TicketRecordEntity();
        new ModelMapper().map(ticketEntity, inputTicketRecord);
        expectedTicketRecord.setPublicId("ticket record public id");
        expectedTicketRecord.setMainTicket(ticketEntity);
        expectedTicketRecord.setCreator(creatorEntity);

        ArgumentCaptor<TicketRecordEntity> ticketRecordCaptor = ArgumentCaptor.forClass(TicketRecordEntity.class);

        when(serviceUtils.getTicketEntity(anyString())).thenReturn(ticketEntity);
        when(modelMapper.map(eq(ticketEntity), eq(TicketRecordEntity.class))).thenReturn(inputTicketRecord);
        when(utils.generateRandomString(anyInt())).thenReturn(expectedTicketRecord.getPublicId());
        when(serviceUtils.getUserEntity(anyString())).thenReturn(creatorEntity);

        ticketService.createTicketRecord(ticketEntity.getPublicId(), creatorEntity.getPublicId());

        verify(ticketRecordRepo).save(ticketRecordCaptor.capture());

        TicketRecordEntity actualTicketRecord = ticketRecordCaptor.getValue();

        assertNotNull(actualTicketRecord);
        assertNull(actualTicketRecord.getId());
        assertEquals(expectedTicketRecord.getPublicId(), actualTicketRecord.getPublicId());
        assertNotNull(actualTicketRecord.getCreatedTime());
        assertEquals(expectedTicketRecord.getMainTicket(), actualTicketRecord.getMainTicket());
        assertEquals(expectedTicketRecord.getCreator(), actualTicketRecord.getCreator());
    }

    @Test
    void getTicketRecords() {
        TicketRecordDTO ticketRecordDTO = new TicketRecordDTO();
        new ModelMapper().map(ticketDTO, ticketRecordDTO);

        DataTablesOutput<TicketRecordEntity> ticketRecordsEntities = new DataTablesOutput<>();

        DataTablesOutput<TicketRecordDTO> ticketRecordsDTOs = new DataTablesOutput<>();
        ticketRecordsDTOs.setDraw(dataTablesInput.getDraw());
        ticketRecordsDTOs.setData(List.of(ticketRecordDTO));

        when(serviceUtils.getTicketEntity(anyString())).thenReturn(ticketEntity);
        when(ticketRecordRepo.findAll(any(DataTablesInput.class),
                ArgumentMatchers.<Specification<TicketRecordEntity>>any()))
                .thenReturn(ticketRecordsEntities);
        when(utils.map(eq(ticketRecordsEntities),
                ArgumentMatchers.<TypeToken<List<TicketRecordDTO>>>any()))
                .thenReturn(ticketRecordsDTOs);

        DataTablesOutput<TicketRecordDTO> actualTicketRecordsDTOs =
                ticketService.getTicketRecords(ticketEntity.getPublicId(), dataTablesInput);


        assertNotNull(actualTicketRecordsDTOs);
        assertEquals(ticketRecordsDTOs, actualTicketRecordsDTOs);
    }


    @Test
    void getTicketRecord() {
        TicketRecordEntity ticketRecordEntity = new TicketRecordEntity();
        new ModelMapper().map(ticketEntity, ticketRecordEntity);

        TicketRecordDTO inputTicketRecordDTO = new TicketRecordDTO();
        new ModelMapper().map(ticketDTO, inputTicketRecordDTO);

        TicketRecordDTO expectedTicketRecordDTO = new TicketRecordDTO();
        new ModelMapper().map(ticketDTO, expectedTicketRecordDTO);


        when(ticketRecordRepo.findByPublicId(anyString())).thenReturn(ticketRecordEntity);
        when(modelMapper.map(any(TicketRecordEntity.class), eq(TicketRecordDTO.class))).thenReturn(inputTicketRecordDTO);

        TicketRecordDTO actualTicketRecordDTO = ticketService.getTicketRecord(ticketRecordEntity.getPublicId());

        assertEqualsTicketRecord(expectedTicketRecordDTO, actualTicketRecordDTO);
    }

    @Test
    void addSubscriber() {
        assert ticketEntity.addSubscriber(creatorEntity);

        TicketEntity inputTicketEntity = new TicketEntity();
        new ModelMapper().map(ticketEntity, inputTicketEntity);
        inputTicketEntity.setSubscribers(new HashSet<>());

        UserEntity inputSubscriberEntity = new UserEntity();
        new ModelMapper().map(creatorEntity, inputSubscriberEntity);
        inputSubscriberEntity.setSubscribedToTickets(new HashSet<>());

        ArgumentCaptor<TicketEntity> ticketEntityCaptor = ArgumentCaptor.forClass(TicketEntity.class);

        when(serviceUtils.getTicketEntity(anyString())).thenReturn(inputTicketEntity);
        when(serviceUtils.getUserEntity(anyString())).thenReturn(inputSubscriberEntity);

        ticketService.addSubscriber(ticketEntity.getPublicId(), creatorEntity.getPublicId());

        verify(ticketRepo).save(ticketEntityCaptor.capture());

        TicketEntity actualTicketEntity = ticketEntityCaptor.getValue();

        assertEqualsTicket(ticketEntity, actualTicketEntity);
        assertEquals(ticketEntity.getSubscribers(), actualTicketEntity.getSubscribers());
    }


    @Test
    void getSubscribers() {
        DataTablesOutput<UserEntity> subscribersEntities = new DataTablesOutput<>();

        DataTablesOutput<UserDTO> subscribersDTOs = new DataTablesOutput<>();
        subscribersDTOs.setDraw(dataTablesInput.getDraw());
        subscribersDTOs.setData(List.of(creatorDTO));

        when(serviceUtils.getTicketEntity(anyString())).thenReturn(ticketEntity);
        when(userRepo.findAll(any(DataTablesInput.class),
                ArgumentMatchers.<Specification<UserEntity>>any()))
                .thenReturn(subscribersEntities);
        when(utils.map(eq(subscribersEntities),
                ArgumentMatchers.<TypeToken<List<UserDTO>>>any()))
                .thenReturn(subscribersDTOs);

        DataTablesOutput<UserDTO> actualSubscribersDTOs =
                ticketService.getSubscribers(ticketEntity.getPublicId(), dataTablesInput);


        assertNotNull(actualSubscribersDTOs);
        assertEquals(subscribersDTOs, actualSubscribersDTOs);
    }

    @Test
    void removeSubscriber() {
        ticketEntity.setSubscribers(new HashSet<>());

        UserEntity inputSubscriberEntity = new UserEntity();
        new ModelMapper().map(creatorEntity, inputSubscriberEntity);

        TicketEntity inputTicketEntity = new TicketEntity();
        new ModelMapper().map(ticketEntity, inputTicketEntity);
        assert inputTicketEntity.addSubscriber(inputSubscriberEntity);

        ArgumentCaptor<TicketEntity> ticketEntityCaptor = ArgumentCaptor.forClass(TicketEntity.class);

        when(serviceUtils.getTicketEntity(anyString())).thenReturn(inputTicketEntity);
        when(serviceUtils.getUserEntity(anyString())).thenReturn(inputSubscriberEntity);

        ticketService.removeSubscriber(ticketEntity.getPublicId(), creatorEntity.getPublicId());

        verify(ticketRepo).save(ticketEntityCaptor.capture());

        TicketEntity actualTicketEntity = ticketEntityCaptor.getValue();

        assertEqualsTicket(ticketEntity, actualTicketEntity);
        assertTrue(actualTicketEntity.getSubscribers().isEmpty());
    }

    @Test
    void createComment() {
        CommentDTO commentDto = new CommentDTO();
        commentDto.setContent("Content");

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setContent(commentDto.getContent());

        String publicId = "publicId";

        ArgumentCaptor<CommentEntity> commentCaptor = ArgumentCaptor.forClass(CommentEntity.class);

        when(modelMapper.map(any(CommentDTO.class), eq(CommentEntity.class))).thenReturn(commentEntity);
        when(utils.generateRandomString(anyInt())).thenReturn(publicId);
        when(serviceUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(serviceUtils.getTicketEntity(anyString())).thenReturn(ticketEntity);

        ticketService.createComment(ticketEntity.getPublicId(), creatorEntity.getPublicId(), commentDto);

        verify(commentRepo).save(commentCaptor.capture());

        CommentEntity actualComment = commentCaptor.getValue();

        assertNotNull(actualComment);
        assertEquals(commentDto.getContent(), actualComment.getContent());
        assertEquals(publicId, actualComment.getPublicId());
        assertEquals(creatorEntity, actualComment.getCreator());
        assertEquals(ticketEntity, actualComment.getTicket());
        assertNotNull(actualComment.getUploadTime());
    }

    @Test
    void getComments() {
        Sort.Direction sort = Sort.Direction.ASC;
        CommentDTO commentDto = new CommentDTO();
        commentDto.setContent("Content");

        CommentEntity commentEntity = new CommentEntity();
        commentEntity.setContent(commentDto.getContent());

        PageImpl<CommentEntity> commentEntities = new PageImpl<>(List.of(commentEntity));
        PageImpl<CommentDTO> commentDTOs = new PageImpl<>(List.of(commentDto), Pageable.ofSize(5), 3);

        when(serviceUtils.getTicketEntity(anyString())).thenReturn(ticketEntity);
        when(commentRepo.findAllByTicket(any(TicketEntity.class), any(PageRequest.class))).thenReturn(commentEntities);
        when(modelMapper.map(ArgumentMatchers.<Page<CommentEntity>>any(), eq(new TypeToken<Page<CommentDTO>>() {
        }.getType())))
                .thenReturn(commentDTOs);

        Page<CommentDTO> actualCommentDTOs =
                ticketService.getComments(projectEntity.getPublicId(), 1, 3, "name", sort);

        assertNotNull(actualCommentDTOs);
        assertEquals(commentDTOs, actualCommentDTOs);
    }

    @Test
    void getTicketsUserSubscribedTo() {
        DataTablesOutput<TicketEntity> dataTablesOutputEntity = new DataTablesOutput<>();
        dataTablesOutputEntity.setDraw(dataTablesInput.getDraw());
        dataTablesOutputEntity.setData(List.of(ticketEntity));

        DataTablesOutput<TicketDTO> dataTablesOutputDTO = new DataTablesOutput<>();
        dataTablesOutputDTO.setDraw(dataTablesInput.getDraw());
        dataTablesOutputDTO.setData(List.of(ticketDTO));

        when(serviceUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(ticketRepo.findAll(any(DataTablesInput.class),
                ArgumentMatchers.<Specification<TicketEntity>>any())).thenReturn(dataTablesOutputEntity);
        when(utils.map(ArgumentMatchers.<DataTablesOutput<TicketEntity>>any(),
                ArgumentMatchers.<TypeToken<List<TicketDTO>>>any())).thenReturn(dataTablesOutputDTO);


        DataTablesOutput<TicketDTO> actualDataTablesOutputDTO =
                ticketService.getTicketsUserSubscribedTo(creatorEntity.getPublicId(), dataTablesInput);


        verify(ticketRepo).findAll(eq(dataTablesInput), ArgumentMatchers.<Specification<TicketEntity>>any());
        verify(utils).map(eq(dataTablesOutputEntity), ArgumentMatchers.<TypeToken<List<TicketDTO>>>any());

        assertNotNull(actualDataTablesOutputDTO);
        assertEquals(dataTablesOutputDTO, actualDataTablesOutputDTO);
    }
}



