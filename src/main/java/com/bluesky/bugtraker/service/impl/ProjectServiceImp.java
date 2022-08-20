package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.io.entity.TicketEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.io.repository.UserRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.*;


@Service
public class ProjectServiceImp implements ProjectService {

    private final ProjectRepository projectRepo;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();
    private final Utils utils;


    @Autowired
    public ProjectServiceImp(ProjectRepository projectRepo, UserRepository userRepository, UserService userService,
                             Utils utils) {
        this.projectRepo = projectRepo;
        this.userRepository = userRepository;
        this.userService = userService;
        this.utils = utils;
    }

    /*
     *  Maybe occur an issue if database contains several projects with same name and creator
     *  which is prohibited by the application criteria
     * */
    ProjectEntity getProjectEntity(String userId, String projectName) {
        com.bluesky.bugtraker.shared.dto.UserDto userDto = userService.getUserById(userId);
        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);

        return projectRepo.findByCreatorAndName(userEntity, projectName)
                .orElseThrow(() -> new ProjectServiceException(NO_RECORD_FOUND, projectName));
    }

    @Override
    public ProjectDto getProject(String userId, String projectName) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);

        return modelMapper.map(projectEntity, ProjectDto.class);
    }

    @Override
    public Set<ProjectDto> getProjects(String userId, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        UserEntity userEntity = modelMapper.map(userService.getUserById(userId), UserEntity.class);

        Set<ProjectEntity> entityPages =
                projectRepo.findAllByCreator(userEntity);

        return modelMapper.map(entityPages, new TypeToken<Set<ProjectDto>>() {
        }.getType());
    }

    @Override
    public ProjectDto createProject(String userId, ProjectDto projectDto) {
        UserEntity creator = modelMapper.map(userService.getUserById(userId), UserEntity.class);

        if (projectRepo.existsByCreatorAndName(creator, projectDto.getName()))
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);

        projectEntity.setPublicId(utils.generateProjectId(30));
        projectEntity.setCreator(creator);


        return modelMapper.map(projectRepo.save(projectEntity), ProjectDto.class);
    }

    @Override
    public ProjectDto setProjectName(String userId, String projectName, ProjectRequestModel projectRequestBody) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);
        projectEntity.setName(projectRequestBody.getName());

        return modelMapper.map(projectRepo.save(projectEntity), ProjectDto.class);
    }

    @Override
    public void deleteProject(String userId, String projectName) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);

        projectRepo.delete(projectEntity);
    }

    @Override
    public void addBug(String userId, String projectName, TicketDto ticketDto) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);

        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);
        ticketEntity.setProject(getProjectEntity(userId, projectName));

        boolean isAdded = projectEntity.addBug(ticketEntity);
        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, ticketEntity.getPublicId());

        projectRepo.save(projectEntity);
    }

    @Override
    public void removeBug(String userId, String projectName, TicketDto ticketDto) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);
        TicketEntity ticketEntity = modelMapper.map(ticketDto, TicketEntity.class);

        boolean isRemoved = projectEntity.removeBug(ticketEntity);
        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, ticketEntity.getPublicId());

        projectRepo.save(projectEntity);
    }

    @Override
    public void addSubscriber(String userId,
                              String projectName,
                              SubscriberRequestModel subscriber) {

        ProjectEntity projectEntity = getProjectEntity(userId, projectName);
        UserDto subscriberDto = userService.getUserById(subscriber.getPublicId());

        boolean isAdded = projectEntity.addSubscriber(
                modelMapper.map(subscriberDto, UserEntity.class));

        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_ADDED, subscriber.getPublicId());
        else
            projectRepo.save(projectEntity);
    }

    @Override
    public Set<UserDto> getSubscribers(String userId, String projectName) {

        Set<ProjectEntity> projects = Set.of(getProjectEntity(userId, projectName));

        Set<UserEntity> subscribersEntity =
                userRepository.findALlBySubscribedToProjectsIn(projects);

        return modelMapper.map(subscribersEntity, new TypeToken<Set<UserDto>>() {}.getType());

    }

    @Override
    public ProjectDto removeSubscriber(String userId, String projectName, String subscriberId) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);

        boolean isRemoved = projectEntity.removeSubscriber(
                modelMapper.map(userService.getUserById(userId), UserEntity.class));

        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, subscriberId);

        return modelMapper.map(
                projectRepo.save(projectEntity), ProjectDto.class);
    }
}









