package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.io.entity.CommentEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.utils.ServiceUtils;
import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.CommentDTO;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Date;

import static com.bluesky.bugtraker.exceptions.ErrorType.*;
import static com.bluesky.bugtraker.service.specifications.Specs.*;


@Service
public class ProjectServiceImp implements ProjectService {
    private final ProjectRepository projectRepo;
    private final CommentRepository commentRepo;
    private final UserRepository userRepository;
    private final ServiceUtils serviceUtils;
    private final Utils utils;
    private final ModelMapper modelMapper;

    @Autowired
    public ProjectServiceImp(ProjectRepository projectRepo,
                             ServiceUtils serviceUtils,
                             UserRepository userRepository,
                             CommentRepository commentRepo,
                             Utils utils, ModelMapper modelMapper) {
        this.projectRepo = projectRepo;
        this.serviceUtils = serviceUtils;
        this.userRepository = userRepository;
        this.commentRepo = commentRepo;
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    @Override
    @NotNull
    public ProjectDTO getProject(@NotNull String projectId) {
        ProjectEntity projectEntity = serviceUtils.getProjectEntity(projectId);

        return modelMapper.map(projectEntity, ProjectDTO.class);
    }

    @Override
    public DataTablesOutput<ProjectDTO> getProjects(String creatorId, DataTablesInput input) {
        UserEntity userEntity = serviceUtils.getUserEntity(creatorId);

        DataTablesOutput<ProjectEntity> all =
                projectRepo.findAll(input, projectByCreator(userEntity));

        return utils.map(all, new TypeToken<>() {});
    }

    @Override
    public void createProject(String creatorId, ProjectDTO projectDto) {
        UserEntity creator = serviceUtils.getUserEntity(creatorId);

        if (projectRepo.existsByCreatorAndName(creator, projectDto.getName()))
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);
        projectEntity.setPublicId(utils.generateProjectId());

        boolean isAdded = projectEntity.setCreator(creator);
        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_ADDED, projectEntity.getName());

        projectRepo.save(projectEntity);
    }

    @Override
    public ProjectDTO updateProject(String projectId, ProjectDTO projectDto) {
        ProjectEntity projectEntity = serviceUtils.getProjectEntity(projectId);

        if (projectRepo.existsByCreatorAndName(projectEntity.getCreator(), projectDto.getName()))
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

        projectEntity.setName(projectDto.getName());

        ProjectEntity savedProject = projectRepo.save(projectEntity);
        return modelMapper.map(savedProject, ProjectDTO.class);
    }

    @Override
    public void deleteProject(@NotNull  String projectId) {
        ProjectEntity projectEntity = serviceUtils.getProjectEntity(projectId);
        boolean isRemoved = projectEntity.removeCreator();

        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, projectId);
        else 
            projectRepo.delete(projectEntity);
    }

    @Override
    public void addSubscriber(String projectId,
                              SubscriberRequestModel subscriber) {

        ProjectEntity projectEntity = serviceUtils.getProjectEntity(projectId);
        UserEntity subscribedEntity = serviceUtils.getUserEntity(subscriber.getPublicId());

        boolean isAdded = projectEntity.addSubscriber(subscribedEntity);

        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_ADDED, subscriber.getPublicId());
        else
            projectRepo.save(projectEntity);
    }

    @Override
    public DataTablesOutput<UserDTO> getSubscribers(String projectId, DataTablesInput input) {
        ProjectEntity projectEntity = serviceUtils.getProjectEntity(projectId);

        DataTablesOutput<UserEntity> all =
                userRepository.findAll(input, allProjectSubscribersByProject(projectEntity));
        return utils.map(all, new TypeToken<>() {});
    }


    @Override
    public void removeSubscriber(String projectId, String subscriberId) {
        ProjectEntity projectEntity = serviceUtils.getProjectEntity(projectId);
        UserEntity userEntity = serviceUtils.getUserEntity(subscriberId);

        boolean isRemoved = projectEntity.removeSubscriber(userEntity);

        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, subscriberId);

        projectRepo.save(projectEntity);
    }

    @Override
    public void createComment(String projectId, String commentCreatorId, CommentDTO comment) {
        CommentEntity commentEntity = modelMapper.map(comment, CommentEntity.class);
        commentEntity.setPublicId(utils.generateRandomString(10));
        commentEntity.setUploadTime(Date.from(Instant.now()));

        UserEntity creator = serviceUtils.getUserEntity(projectId);
        ProjectEntity projectEntity = serviceUtils.getProjectEntity(projectId);

        boolean isCreatorAdded = commentEntity.setCreator(creator);
        if (!isCreatorAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, commentCreatorId);

        boolean isProjectAdded = commentEntity.addProject(projectEntity);
        if (!isProjectAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, projectId);

        commentRepo.save(commentEntity);
    }


    @Override
    public Page<CommentDTO> getComments(String projectId,
                                        int page, int limit,
                                        String sortBy, Sort.Direction dir) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        ProjectEntity projectEntity = serviceUtils.getProjectEntity(projectId);

        PageRequest pageRequest = PageRequest.of(page - 1, limit, dir, sortBy);


        Page<CommentEntity> commentEntities =
                commentRepo.findAllByProject(projectEntity, pageRequest);

        return modelMapper.map(commentEntities, new TypeToken<Page<CommentDTO>>() {
        }.getType());
    }

    @Override
    public DataTablesOutput<ProjectDTO> getSubscribedToProjects(String userId, DataTablesInput input) {
        UserEntity userEntity = serviceUtils.getUserEntity(userId);

        DataTablesOutput<ProjectEntity> subscribedToProjects =
                projectRepo.findAll(input, projectBySubscriber(userEntity));

        return utils.map(subscribedToProjects, new TypeToken<>() {});
    }
    
}









