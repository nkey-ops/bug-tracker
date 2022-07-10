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

import java.util.*;

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


    private Optional<ProjectDto> getProjectOptional(String userId, String projectName) {
        UserDto userDto = userService.getUserById(userId);

        projectRepo.findByCreatorAndName(userDto.getId(), projectName);
//        return userDto.getProjects()
//                .stream()
//                .filter(project -> project.getName().equals(projectName))
//                .findFirst();
    }

    @Override
    public ProjectDto getProject(String userId, String projectName) {
        return getProjectOptional(userId, projectName)
                .orElseThrow(() -> new ProjectServiceException(NO_RECORD_FOUND, projectName));
    }

    @Override
    public Set<ProjectDto> getProjects(String userId, int page, int limit) {
        if (page-- < 1 || limit < 1) throw new IllegalArgumentException();

        UserEntity userEntity = modelMapper.map(userService.getUserById(userId), UserEntity.class);

        Page<ProjectEntity> entityPages = projectRepo.findAllByCreator(userEntity, PageRequest.of(page, limit));
        List<ProjectEntity> content = entityPages.getContent();

        return modelMapper.map(content, new TypeToken<Set<ProjectDto>>() {
        }.getType());
    }

    @Override
    public ProjectDto createProject(String userId, ProjectDto projectDto) {
        if (getProjectOptional(userId, projectDto.getName()).isPresent())
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

        projectDto.setPublicId(utils.generateProjectId(30));
        projectDto.setCreator(userService.getUserById(userId));

        ProjectEntity savedProject = projectRepo.save(
                modelMapper.map(projectDto, ProjectEntity.class));


        return modelMapper.map(savedProject, ProjectDto.class);
    }

    @Override
    public ProjectDto setProjectName(String userId, String oldName, ProjectRequestBody projectRequestBody) {
        ProjectDto projectDto = getProject(userId, oldName);
        projectDto.setName(projectRequestBody.getName());

        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);

        return modelMapper.map(projectRepo.save(projectEntity), ProjectDto.class);
    }

    @Override
    public void deleteProject(String userId, String projectName) {
        ProjectDto projectDto = getProject(userId, projectName);

        projectRepo.delete(modelMapper.map(projectDto, ProjectEntity.class));
    }

    @Override
    public void addBug(String userId, String projectName, BugDto bugDto) {
        ProjectDto project = getProject(userId, projectName);

        ProjectEntity projectEntity = modelMapper.map(project, ProjectEntity.class);
        BugEntity bugEntity = modelMapper.map(bugDto, BugEntity.class);

        boolean isAdded = projectEntity.addBug(bugEntity);
        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_EXISTS, bugEntity.getPublicId());

        projectRepo.save(projectEntity);
    }

    @Override
    public void removeBug(String userId, String projectName, BugDto bugDto) {
        ProjectDto project = getProject(userId, projectName);

        ProjectEntity projectEntity = modelMapper.map(project, ProjectEntity.class);
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
        ProjectDto projectDto = getProject(userId, projectName);

        boolean isAdded = projectDto.getSubscribers().add(userService.getUserById(subscriberId));

        if (!isAdded)
            throw new ProjectServiceException(RECORD_ALREADY_ADDED, subscriberId);

        ProjectEntity updatedProject = modelMapper.map(projectDto, ProjectEntity.class);


        return modelMapper.map(projectRepo.save(updatedProject), ProjectDto.class);
    }

    @Override
    public Set<UserDto> getSubscribers(String userId, String projectName, int page, int limit) {
        if (page < 1 || limit < 1) throw new IllegalArgumentException();

        ProjectDto projectDto = getProject(userId, projectName);

        PageImpl<UserDto> pagedUsers = new PageImpl<>(
                new ArrayList<>(projectDto.getSubscribers()),
                Pageable.ofSize(page), limit);

        return new LinkedHashSet<>(pagedUsers.getContent());
    }

    @Override
    public ProjectDto removeSubscriber(String userId, String projectName, String subscriberId) {
        ProjectDto projectDto = getProject(userId, projectName);
        boolean isRemoved = projectDto.getSubscribers().remove((userService.getUserById(subscriberId)));

        if (!isRemoved)
            throw new ProjectServiceException(NO_RECORD_FOUND, subscriberId);

        return modelMapper.map(
                projectRepo.save(modelMapper.map(projectDto, ProjectEntity.class)),
                ProjectDto.class);
    }
}









