package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.exceptions.serviceexception.TicketServiceException;
import com.bluesky.bugtraker.io.entity.CommentEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.CommentRepository;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
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

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.*;


@Service
public class ProjectServiceImp implements ProjectService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepository;
    private final CommentRepository commentRepo;
    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();
    private final Utils utils;


    @Autowired
    public ProjectServiceImp(ProjectRepository projectRepo, UserRepository userRepository, CommentRepository commentRepo, UserService userService,
                             Utils utils) {
        this.projectRepo = projectRepo;
        this.userRepository = userRepository;
        this.commentRepo = commentRepo;
        this.userService = userService;
        this.utils = utils;
    }

    /*
     *  Maybe occur an issue if database contains several projects with same name and creator
     *  which is prohibited by the application criteria
     * */
    ProjectEntity getProjectEntity(String projectId) {
        return projectRepo.findByPublicId(projectId)
                .orElseThrow(() -> new ProjectServiceException(NO_RECORD_FOUND, projectId));
    }

    @Override
    public ProjectDto getProject(String projectId) {
        ProjectEntity projectEntity = getProjectEntity(projectId);

        return modelMapper.map(projectEntity, ProjectDto.class);
    }

    //    @Override
//    public Set<ProjectDto> getProjects(String userId, int page, int limit) {
//        if (page < 1 || limit < 1) throw new IllegalArgumentException();
//
//        UserEntity userEntity = modelMapper.map(userService.getUserById(userId), UserEntity.class);
//
//        Set<ProjectEntity> entityPages =
//                projectRepo.findAllByCreator(userEntity);
//
//        return modelMapper.map(entityPages, new TypeToken<Set<ProjectDto>>() {
//        }.getType());
//    }

    @Override
    public DataTablesOutput<ProjectDto> getProjects(DataTablesInput input) {


        DataTablesOutput<ProjectEntity> all = projectRepo.findAll(input);

        DataTablesOutput<ProjectDto> result = new DataTablesOutput<>();

        modelMapper.map(all, result);
        result.setData(modelMapper.map(all.getData(), new TypeToken<List<ProjectDto>>() {
        }.getType()));
        
        return result;
    }
    
//    TODO remove
//    @Override
//    public Page<ProjectDto> getProjects(String userId,
//                                        int page, int limit,
//                                        Sort.Direction dir, String sortBy) {
//        if (page < 0 || limit < 1) throw new IllegalArgumentException();
//
//        PageRequest pageRequest =
//                PageRequest.of(page, limit, dir, sortBy);
//
//        Page<ProjectEntity> pagedProjectEntities =
//                projectRepo.findAll(pageRequest);
//        
//        return  pagedProjectEntities.map(projectEntity -> modelMapper.map(projectEntity, ProjectDto.class));
//    }

    @Override
    public ProjectDto createProject(String creatorId, ProjectDto projectDto) {
        UserEntity creator = modelMapper.map(userService.getUserById(creatorId), UserEntity.class);

        if (projectRepo.existsByCreatorAndName(creator, projectDto.getName()))
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);

        projectEntity.setPublicId(utils.generateProjectId(30));
        projectEntity.setCreator(creator);


        return modelMapper.map(projectRepo.save(projectEntity), ProjectDto.class);
    }

    @Override
    public ProjectDto setProjectName(String projectId, ProjectRequestModel projectRequestBody) {
        ProjectEntity projectEntity = getProjectEntity(projectId);
        projectEntity.setName(projectRequestBody.getName());

        return modelMapper.map(projectRepo.save(projectEntity), ProjectDto.class);
    }

    @Override
    public void deleteProject(String projectId) {
        ProjectEntity projectEntity = getProjectEntity(projectId);

        projectRepo.delete(projectEntity);
    }

    @Override
    public void addTicket(String projectId, TicketDto ticketDto) {
        ProjectEntity projectEntity = getProjectEntity(projectId);

        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);
        ticketEntity.setProject(getProjectEntity(projectId));

        boolean isAdded = projectEntity.addTicket(ticketEntity);
        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, ticketEntity.getPublicId());

        projectRepo.save(projectEntity);
    }

    @Override
    public void removeTicket(String projectId, TicketDto ticketDto) {
        ProjectEntity projectEntity = getProjectEntity(projectId);
        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);

        boolean isRemoved = projectEntity.removeTicket(ticketEntity);
        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, ticketEntity.getPublicId());

        projectRepo.save(projectEntity);
    }

    @Override
    public void addSubscriber(String projectId,
                              SubscriberRequestModel subscriber) {

        ProjectEntity projectEntity = getProjectEntity(projectId);
        UserDto subscriberDto = userService.getUserById(subscriber.getPublicId());

        boolean isAdded = projectEntity.addSubscriber(
                modelMapper.map(subscriberDto, UserEntity.class));

        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_ADDED, subscriber.getPublicId());
        else
            projectRepo.save(projectEntity);
    }

    @Override
    public DataTablesOutput<UserDto> getSubscribers(String projectId, DataTablesInput input) {

        Set<ProjectEntity> projects = Set.of(getProjectEntity(projectId));

        DataTablesOutput<UserEntity> all = userRepository.findAll(input);
        
        DataTablesOutput<UserDto> result = new DataTablesOutput<>();

        modelMapper.map(all, result);
        result.setData(modelMapper.map(all.getData(), new TypeToken<List<ProjectDto>>() {
        }.getType()));

        return result;
        
    }


    @Override
    public ProjectDto removeSubscriber(String userId, String projectId, String subscriberId) {
        ProjectEntity projectEntity = getProjectEntity(projectId);

        boolean isRemoved = projectEntity.removeSubscriber(
                modelMapper.map(userService.getUserById(userId), UserEntity.class));

        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, subscriberId);

        return modelMapper.map(
                projectRepo.save(projectEntity), ProjectDto.class);
    }

    @Override
    public void createComment(String projectId, String commentCreatorId, CommentDto comment) {
        CommentEntity commentEntity = modelMapper.map(comment, CommentEntity.class);

        commentEntity.setPublicId(utils.generateCommentId(10));
        commentEntity.setUploadTime(Date.from(Instant.now()));

        UserEntity creator = modelMapper.map(userService.getUserById(commentCreatorId), UserEntity.class);
        ProjectEntity projectEntity = this.getProjectEntity(projectId);

        boolean isCreatorAdded = commentEntity.addCreator(creator);
        if (!isCreatorAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, commentCreatorId);

        boolean isProjectAdded = commentEntity.addProject(projectEntity);
        if (!isProjectAdded)
            throw new TicketServiceException(RECORD_ALREADY_ADDED, projectId);

        commentRepo.save(commentEntity);
    }


    @Override
    public Page<CommentDto> getComments(String projectId,
                                        int page, int limit,
                                        String sortBy, Sort.Direction dir) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        ProjectEntity projectEntity = this.getProjectEntity(projectId);

        PageRequest pageRequest =
                PageRequest.of(page - 1, limit, dir, sortBy);


        Page<CommentEntity> commentEntities =
                commentRepo.findAllByProject(projectEntity, pageRequest);

        return modelMapper.map(commentEntities, new TypeToken<Page<CommentDto>>() {
        }.getType());
    }

    
}









