package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.io.entity.CommentEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.service.utils.DataExtractionUtils;
import com.bluesky.bugtraker.shared.dto.CommentDTO;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProjectServiceImpTest {
    @InjectMocks
    private ProjectServiceImp projectService;

    @Mock
    private ProjectRepository projectRepo;
    @Mock
    private CommentRepository commentRepo;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DataExtractionUtils dataExtractionUtils;
    @Mock
    private Utils utils;
    @Mock
    private ModelMapper modelMapper;


    private UserDTO creatorDTO;
    private UserEntity creatorEntity;
    private ProjectDTO projectDTO;
    private ProjectEntity projectEntity;
    private DataTablesInput dataTablesInput;

    @BeforeEach
    void setUp() {
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


        dataTablesInput = new DataTablesInput();
        dataTablesInput.setStart(1);
        dataTablesInput.setLength(1);
        dataTablesInput.setDraw(1);
    }


    @Test
    void getProject() {
        when(dataExtractionUtils.getProjectEntity(anyString())).thenReturn(projectEntity);
        when(modelMapper.map(any(ProjectEntity.class), eq(ProjectDTO.class))).thenReturn(projectDTO);

        ProjectDTO actualProject = projectService.getProject(projectDTO.getPublicId());

        verify(dataExtractionUtils, times(1)).getProjectEntity(anyString());
        verify(modelMapper, times(1)).map(any(ProjectEntity.class), eq(ProjectDTO.class));

        assertNotNull(actualProject);
        assertEquals(projectDTO, actualProject);
    }

    @Test
    void getProjects() {
        DataTablesOutput<ProjectDTO> dataTablesOutputDTO = new DataTablesOutput<>();
        dataTablesOutputDTO.setDraw(dataTablesInput.getDraw());
        dataTablesOutputDTO.setData(List.of(projectDTO));

        DataTablesOutput<ProjectEntity> dataTablesOutputEntity = new DataTablesOutput<>();
        dataTablesOutputEntity.setDraw(dataTablesInput.getDraw());
        dataTablesOutputEntity.setData(List.of(projectEntity));

        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(projectRepo.findAll(any(DataTablesInput.class), ArgumentMatchers.<Specification<ProjectEntity>>any()))
                .thenReturn(dataTablesOutputEntity);
        when(utils.map(any(DataTablesOutput.class), ArgumentMatchers.<TypeToken<List<ProjectDTO>>>any()))
                .thenReturn(dataTablesOutputDTO);

        DataTablesOutput<ProjectDTO> actualDatatablesOutput =
                projectService.getProjects(projectDTO.getCreator().getPublicId(), dataTablesInput);

        verify(dataExtractionUtils, times(1)).getUserEntity(anyString());
        verify(projectRepo, times(1)).findAll(any(DataTablesInput.class),
                ArgumentMatchers.<Specification<ProjectEntity>>any());
        verify(utils, times(1)).map(any(DataTablesOutput.class),
                ArgumentMatchers.<TypeToken<List<ProjectDTO>>>any());

        assertNotNull(actualDatatablesOutput);
        assertEquals(dataTablesOutputDTO, actualDatatablesOutput);
    }

    @Test
    void createProject() {
        assertTrue(projectEntity.removeCreator());

        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(projectRepo.existsByCreatorAndName(any(UserEntity.class), anyString())).thenReturn(false);
        when(modelMapper.map(any(ProjectDTO.class), eq(ProjectEntity.class))).thenReturn(projectEntity);
        when(utils.generateProjectId()).thenReturn(projectEntity.getPublicId());

        projectService.createProject(creatorEntity.getPublicId(), projectDTO);

        verify(dataExtractionUtils, times(1)).getUserEntity(anyString());
        verify(projectRepo, times(1)).existsByCreatorAndName(any(UserEntity.class), anyString());
        verify(modelMapper, times(1)).map(any(ProjectDTO.class), eq(ProjectEntity.class));
        verify(utils, times(1)).generateProjectId();
        verify(projectRepo, times(1)).save(any(ProjectEntity.class));
    }

    @Test
    void createProjectThrowsWhenProjectWithTheSameNameAlreadyExists() {
        assertTrue(projectEntity.removeCreator());

        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(projectRepo.existsByCreatorAndName(any(UserEntity.class), anyString())).thenReturn(true);

        assertThrows(ProjectServiceException.class, () ->
                projectService.createProject(creatorEntity.getPublicId(), projectDTO));

        verify(dataExtractionUtils, times(1)).getUserEntity(anyString());
        verify(projectRepo, times(1)).existsByCreatorAndName(any(UserEntity.class), anyString());
    }

    @Test
    void createProjectThrowsWhenProjectAlreadyAddedToCreator() {
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(projectRepo.existsByCreatorAndName(any(UserEntity.class), anyString())).thenReturn(false);
        when(modelMapper.map(any(ProjectDTO.class), eq(ProjectEntity.class))).thenReturn(projectEntity);
        when(utils.generateProjectId()).thenReturn(projectEntity.getPublicId());

        assertThrows(ProjectServiceException.class, () ->
                projectService.createProject(creatorEntity.getPublicId(), projectDTO));

        verify(dataExtractionUtils, times(1)).getUserEntity(anyString());
        verify(projectRepo, times(1)).existsByCreatorAndName(any(UserEntity.class), anyString());
        verify(modelMapper, times(1)).map(any(ProjectDTO.class), eq(ProjectEntity.class));
        verify(utils, times(1)).generateProjectId();
    }

    @Test
    void updateProject() {
        String newName = "newProjectName";

        ProjectDTO projectDTOInput = new ProjectDTO();
        projectDTOInput.setName(newName);

        ProjectEntity projectEntityOutput = new ProjectEntity();
        new ModelMapper().map(projectEntity, projectEntityOutput);
        projectEntityOutput.setName(newName);
      
        ProjectDTO projectDTOOutput = new ProjectDTO();
        new ModelMapper().map(projectDTO, projectDTOOutput);
        projectDTOOutput.setName(newName);

        
        when(dataExtractionUtils.getProjectEntity(anyString())).thenReturn(projectEntity);
        when(projectRepo.existsByCreatorAndName(any(UserEntity.class), anyString())).thenReturn(false);
        when(projectRepo.save(projectEntityOutput)).thenReturn(projectEntityOutput);
        when(modelMapper.map(any(ProjectEntity.class), eq(ProjectDTO.class))).thenReturn(projectDTOOutput);
      
        ProjectDTO actualProjectDTO = projectService.updateProject(projectDTO.getPublicId(), projectDTOInput);
        
         verify(projectRepo).save(projectEntity);

        assertNotNull(actualProjectDTO);
        assertEquals(projectDTOOutput, actualProjectDTO);
    }

    @Test
    void deleteProject() {
        ProjectEntity expected = new ProjectEntity();
        new ModelMapper().map(projectEntity, expected);

        ProjectEntity input = new ProjectEntity();
        input.setCreator(creatorEntity);

        ArgumentCaptor<ProjectEntity> projectEntityCapture = ArgumentCaptor.forClass(ProjectEntity.class);

        when(dataExtractionUtils.getProjectEntity(anyString())).thenReturn(projectEntity);

        projectService.deleteProject(projectDTO.getPublicId());

        verify(projectRepo).delete(projectEntityCapture.capture());

        ProjectEntity actualProjectEntity = projectEntityCapture.getValue();

        assertNotNull(actualProjectEntity);
        assertEquals(expected, actualProjectEntity);
    }

    @Test
    void addSubscriber() {
        SubscriberRequestModel subscriber = new SubscriberRequestModel();
        subscriber.setPublicId(creatorEntity.getPublicId());

        ArgumentCaptor<ProjectEntity> projectEntityCaptor = ArgumentCaptor.forClass(ProjectEntity.class);

        when(dataExtractionUtils.getProjectEntity(anyString())).thenReturn(new ProjectEntity());
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(creatorEntity);

        projectService.addSubscriber(projectEntity.getPublicId(), subscriber);

        verify(projectRepo).save(projectEntityCaptor.capture());


        ProjectEntity actualProjectEntity = projectEntityCaptor.getValue();
        assertNotNull(actualProjectEntity);

        Set<UserEntity> subscribers = actualProjectEntity.getSubscribers();
        assertEquals(1, subscribers.size());
        assertTrue(subscribers.contains(creatorEntity));
    }

    @Test
    void getSubscribers() {
        DataTablesOutput<UserEntity> dataTablesOutputEntity = new DataTablesOutput<>();
        dataTablesOutputEntity.setData(List.of(creatorEntity));

        DataTablesOutput<UserDTO> dataTablesOutputDTO = new DataTablesOutput<>();
        dataTablesOutputDTO.setData(List.of(creatorDTO));


        when(dataExtractionUtils.getProjectEntity(anyString())).thenReturn(projectEntity);
        when(userRepository.findAll(any(DataTablesInput.class), ArgumentMatchers.<Specification<UserEntity>>any()))
                .thenReturn(dataTablesOutputEntity);
        when(utils.map(eq(dataTablesOutputEntity), ArgumentMatchers.<TypeToken<List<UserDTO>>>any()))
                .thenReturn(dataTablesOutputDTO);

        DataTablesOutput<UserDTO> actualSubscribers =
                projectService.getSubscribers(projectEntity.getPublicId(), dataTablesInput);

        verify(dataExtractionUtils).getProjectEntity(anyString());
        verify(userRepository).findAll(any(DataTablesInput.class), ArgumentMatchers.<Specification<UserEntity>>any());
        verify(utils).map(eq(dataTablesOutputEntity), ArgumentMatchers.<TypeToken<List<UserDTO>>>any());

        assertNotNull(actualSubscribers);
        assertEquals(dataTablesOutputDTO, actualSubscribers);
    }

    @Test
    void removeSubscriber() {
        ArgumentCaptor<ProjectEntity> projectEntityCaptor = ArgumentCaptor.forClass(ProjectEntity.class);

        UserEntity subscriber = new UserEntity();
        subscriber.setId(1L);
        subscriber.setPublicId("1");

        assert projectEntity.addSubscriber(subscriber);

        when(dataExtractionUtils.getProjectEntity(anyString())).thenReturn(projectEntity);
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(subscriber);

        projectService.removeSubscriber(projectEntity.getPublicId(), subscriber.getPublicId());

        verify(projectRepo).save(projectEntityCaptor.capture());

        ProjectEntity actualProjectEntity = projectEntityCaptor.getValue();
        assertNotNull(actualProjectEntity);

        Set<UserEntity> subscribers = actualProjectEntity.getSubscribers();
        assertNotNull(subscribers);
        assertEquals(0, subscribers.size());
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
        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(dataExtractionUtils.getProjectEntity(anyString())).thenReturn(projectEntity);

        projectService.createComment(projectEntity.getPublicId(), creatorEntity.getPublicId(), commentDto);


        verify(commentRepo).save(commentCaptor.capture());

        CommentEntity actualComment = commentCaptor.getValue();

        assertNotNull(actualComment);
        assertEquals(commentDto.getContent(), actualComment.getContent());
        assertEquals(publicId, actualComment.getPublicId());
        assertEquals(creatorEntity, actualComment.getCreator());
        assertEquals(projectEntity, actualComment.getProject());
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

        when(dataExtractionUtils.getProjectEntity(anyString())).thenReturn(projectEntity);
        when(commentRepo.findAllByProject(any(ProjectEntity.class), any(PageRequest.class)))
                .thenReturn(commentEntities);
        when(modelMapper.map(ArgumentMatchers.<Page<CommentEntity>>any(), eq(new TypeToken<Page<CommentDTO>>() {
        }.getType())))
                .thenReturn(commentDTOs);

        Page<CommentDTO> actualCommentDTOs =
                projectService.getComments(projectEntity.getPublicId(), 1, 3, "name", sort);

        assertNotNull(actualCommentDTOs);
        assertEquals(commentDTOs, actualCommentDTOs);
    }

    @Test
    void getSubscribedToProjects() {
        DataTablesOutput<ProjectEntity> dataTablesOutputEntity = new DataTablesOutput<>();
        dataTablesOutputEntity.setData(List.of(projectEntity));

        DataTablesOutput<ProjectDTO> dataTablesOutputDTO = new DataTablesOutput<>();
        dataTablesOutputDTO.setData(List.of(projectDTO));

        when(dataExtractionUtils.getUserEntity(anyString())).thenReturn(creatorEntity);
        when(projectRepo.findAll(any(DataTablesInput.class), ArgumentMatchers.<Specification<ProjectEntity>>any()))
                .thenReturn(dataTablesOutputEntity);

        when(utils.map(ArgumentMatchers.<DataTablesOutput<ProjectEntity>>any(),
                ArgumentMatchers.<TypeToken<List<ProjectDTO>>>any())).thenReturn(dataTablesOutputDTO);

        DataTablesOutput<ProjectDTO> actualProjectsDTO =
                projectService.getSubscribedToProjects(creatorEntity.getPublicId(), dataTablesInput);

        assertNotNull(actualProjectsDTO);
        assertEquals(dataTablesOutputDTO, actualProjectsDTO);
    }
}