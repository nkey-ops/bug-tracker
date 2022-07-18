package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.io.entity.BugEntity;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.BugDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.request.ProjectRequestBody;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.*;

@Service
public class ProjectServiceImp implements ProjectService {
    private final ProjectRepository projectRepo;
    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();
    private final Utils utils;


    @Autowired
    public ProjectServiceImp(ProjectRepository projectRepo, UserService userService,
                             Utils utils) {
        this.projectRepo = projectRepo;
        this.userService = userService;
        this.utils = utils;
    }

    /*
     *  Maybe occur an issue if database contains several projects with same name and creator
     *  which is prohibited by the application criteria
     * */
    ProjectEntity getProjectEntity(String userId, String projectName) {
        UserDto userDto = userService.getUserById(userId);
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
        if (page-- < 1 || limit < 1) throw new IllegalArgumentException();

        UserEntity userEntity = modelMapper.map(userService.getUserById(userId), UserEntity.class);

        Page<ProjectEntity> entityPages =
                projectRepo.findAllByCreator(userEntity, PageRequest.of(page, limit));
        List<ProjectEntity> content = entityPages.getContent();

        return modelMapper.map(content, new TypeToken<Set<ProjectDto>>() {
        }.getType());
    }

    @Override
    public ProjectDto createProject(String userId, ProjectDto projectDto) {
        UserEntity creator = modelMapper.map(userService.getUserById(userId), UserEntity.class);

        if(projectRepo.existsByCreatorAndName(creator, projectDto.getName()))
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);

        projectEntity.setPublicId(utils.generateProjectId(30));
        projectEntity.setCreator(creator);


        return modelMapper.map(projectRepo.save(projectEntity), ProjectDto.class);
    }

    @Override
    public ProjectDto setProjectName(String userId, String projectName, ProjectRequestBody projectRequestBody) {
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
    public void addBug(String userId, String projectName, BugDto bugDto) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);
        BugEntity bugEntity = modelMapper.map(bugDto, BugEntity.class);

        boolean isAdded = projectEntity.addBug(bugEntity);
        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, bugEntity.getPublicId());

        projectRepo.save(projectEntity);
    }

    @Override
    public void removeBug(String userId, String projectName, BugDto bugDto) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);
        BugEntity bugEntity = modelMapper.map(bugDto, BugEntity.class);

        boolean isRemoved = projectEntity.removeBug(bugEntity);
        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, bugEntity.getPublicId());

        projectRepo.save(projectEntity);
    }

    @Override
    public ProjectDto addSubscriber(String userId,
                                    String projectName,
                                    String subscriberId) {
        ProjectEntity projectEntity = getProjectEntity(userId, projectName);

        boolean isAdded = projectEntity.addSubscriber(
                modelMapper.map(userService.getUserById(userId), UserEntity.class));

        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_ADDED, subscriberId);

        return modelMapper.map(
                projectRepo.save(projectEntity), ProjectDto.class);
    }

    @Override
    public Set<UserDto> getSubscribers(String userId, String projectName, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        Set<UserDto> subscribers = getProject(userId, projectName).getSubscribers();

        PageImpl<UserDto> pagedUsers =
                new PageImpl<>(
                    subscribers.stream().toList(),
                    Pageable.ofSize(page), limit);


        return new LinkedHashSet<>(pagedUsers.getContent());
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









