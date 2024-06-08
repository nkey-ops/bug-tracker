package com.bluesky.bugtraker.service.impl;

import static com.bluesky.bugtraker.exceptions.ErrorType.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorType.RECORD_ALREADY_ADDED;
import static com.bluesky.bugtraker.exceptions.ErrorType.RECORD_ALREADY_EXISTS;
import static com.bluesky.bugtraker.io.specification.Specs.allProjectSubscribersByProject;
import static com.bluesky.bugtraker.io.specification.Specs.projectByCreator;
import static com.bluesky.bugtraker.io.specification.Specs.projectsBySubscriber;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.io.entity.CommentEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.utils.DataExtractionUtils;
import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.CommentDTO;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImp implements ProjectService {
  private final ProjectRepository projectRepo;
  private final CommentRepository commentRepo;
  private final UserRepository userRepository;
  private final DataExtractionUtils dataExtractionUtils;
  private final Utils utils;
  private final ModelMapper modelMapper;

  @Autowired
  public ProjectServiceImp(
      ProjectRepository projectRepo,
      DataExtractionUtils dataExtractionUtils,
      UserRepository userRepository,
      CommentRepository commentRepo,
      Utils utils,
      ModelMapper modelMapper) {
    this.projectRepo = projectRepo;
    this.dataExtractionUtils = dataExtractionUtils;
    this.userRepository = userRepository;
    this.commentRepo = commentRepo;
    this.utils = utils;
    this.modelMapper = modelMapper;
  }

  @Override
  @NotNull
  public ProjectDTO getProject(@NotNull String projectId) {
    ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);

    return modelMapper.map(projectEntity, ProjectDTO.class);
  }

  @Override
  public DataTablesOutput<ProjectDTO> getProjects(String creatorId, DataTablesInput input) {
    UserEntity userEntity = dataExtractionUtils.getUserEntity(creatorId);

    DataTablesOutput<ProjectEntity> all = projectRepo.findAll(input, projectByCreator(userEntity));

    return utils.map(all, new TypeToken<>() {});
  }

  @Override
  public void createProject(String creatorId, ProjectDTO projectDto) {
    UserEntity creator = dataExtractionUtils.getUserEntity(creatorId);

    if (projectRepo.existsByCreatorAndName(creator, projectDto.getName()))
      throw new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());
    ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);
    projectEntity.setPublicId(utils.generateProjectId());

    boolean isAdded = projectEntity.setCreator(creator);
    if (!isAdded) throw new ProjectServiceException(RECORD_ALREADY_ADDED, projectEntity.getName());

    projectRepo.save(projectEntity);
  }

  @Override
  public ProjectDTO updateProject(String projectId, ProjectDTO projectDto) {
    ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);

    if (projectRepo.existsByCreatorAndName(projectEntity.getCreator(), projectDto.getName()))
      throw new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

    projectEntity.setName(projectDto.getName());

    ProjectEntity savedProject = projectRepo.save(projectEntity);
    return modelMapper.map(savedProject, ProjectDTO.class);
  }

  @Override
  public void deleteProject(@NotNull String projectId) {
    ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);

    projectRepo.delete(projectEntity);
  }

  @Override
  public void addSubscriber(String projectId, SubscriberRequestModel subscriber) {

    ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);
    UserEntity subscribedEntity = dataExtractionUtils.getUserEntity(subscriber.getPublicId());

    boolean isAdded = projectEntity.addSubscriber(subscribedEntity);

    if (!isAdded) throw new ProjectServiceException(RECORD_ALREADY_ADDED, subscriber.getPublicId());
    else projectRepo.save(projectEntity);
  }

  @Override
  public DataTablesOutput<UserDTO> getSubscribers(String projectId, DataTablesInput input) {
    ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);

    DataTablesOutput<UserEntity> all =
        userRepository.findAll(input, allProjectSubscribersByProject(projectEntity));
    return utils.map(all, new TypeToken<>() {});
  }

  @Override
  public void removeSubscriber(String projectId, String subscriberId) {
    ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);
    UserEntity userEntity = dataExtractionUtils.getUserEntity(subscriberId);

    boolean isRemoved = projectEntity.removeSubscriber(userEntity);

    if (!isRemoved) throw new ProjectServiceException(NO_RECORD_FOUND, subscriberId);

    projectRepo.save(projectEntity);
  }

  @Override
  public void createComment(String projectId, String commentCreatorId, CommentDTO comment) {
    CommentEntity commentEntity = modelMapper.map(comment, CommentEntity.class);
    commentEntity.setPublicId(utils.generateRandomString(10));
    commentEntity.setUploadTime(Date.from(Instant.now()));

    UserEntity creator = dataExtractionUtils.getUserEntity(commentCreatorId);
    ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);

    boolean isCreatorAdded = commentEntity.setCreator(creator);
    if (!isCreatorAdded) throw new TicketServiceException(RECORD_ALREADY_ADDED, commentCreatorId);

    boolean isProjectAdded = commentEntity.addProject(projectEntity);
    if (!isProjectAdded) throw new TicketServiceException(RECORD_ALREADY_ADDED, projectId);

    commentRepo.save(commentEntity);
  }

  @Override
  public Page<CommentDTO> getComments(
      String projectId, int page, int limit, String sortBy, Sort.Direction dir) {
    if (page < 1 || limit < 1) throw new IllegalArgumentException();

    ProjectEntity projectEntity = dataExtractionUtils.getProjectEntity(projectId);

    PageRequest pageRequest = PageRequest.of(page - 1, limit, dir, sortBy);

    Page<CommentEntity> commentEntities = commentRepo.findAllByProject(projectEntity, pageRequest);

    return modelMapper.map(commentEntities, new TypeToken<Page<CommentDTO>>() {}.getType());
  }

  @Override
  public DataTablesOutput<ProjectDTO> getSubscribedToProjects(
      String userId, DataTablesInput input) {
    UserEntity userEntity = dataExtractionUtils.getUserEntity(userId);

    DataTablesOutput<ProjectEntity> subscribedToProjects =
        projectRepo.findAll(input, projectsBySubscriber(userEntity));

    return utils.map(subscribedToProjects, new TypeToken<>() {});
  }
}
