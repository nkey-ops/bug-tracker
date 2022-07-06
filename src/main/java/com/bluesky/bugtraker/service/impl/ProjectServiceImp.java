package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.Utils;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.request.ProjectRequestBody;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorMessages.RECORD_ALREADY_EXISTS;

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


    private Optional<ProjectDto> getProjectOptional(String userId, String projectName){
        UserDto userDto = userService.getUserById(userId);

        return userDto.getProjects()
                .stream()
                .filter(project -> project.getName().equals(projectName))
                .findFirst();
    }

    @Override
    public ProjectDto getProject(String userId, String projectName) {
        UserDto userDto = userService.getUserById(userId);

        return  getProjectOptional(userId, projectName)
                .orElseThrow(() -> new ProjectServiceException(NO_RECORD_FOUND, projectName));
    }

    @Override
    public Set<ProjectDto> getProjects(String userId, int page, int limit) {
        if(page-- < 0 || limit < 1) throw new IllegalArgumentException();

        UserEntity userEntity = modelMapper.map(userService.getUserById(userId), UserEntity.class);

        Page<ProjectEntity> entityPages = projectRepo.findAllByCreator(userEntity, PageRequest.of(page, limit));
        List<ProjectEntity> content = entityPages.getContent();

        return modelMapper.map(content, new TypeToken<Set<ProjectDto>>() {}.getType());
    }

    @Override
    public ProjectDto createProject(String userId, ProjectDto projectDto) {
        if (getProjectOptional(userId, projectDto.getName()).isPresent())
            throw  new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

        UserDto userDto = userService.getUserById(userId);

        ProjectDto newProjectDto = new ProjectDto();
        newProjectDto.setPublicId(utils.generateProjectId(30));
        newProjectDto.setName(projectDto.getName());
        newProjectDto.setCreator(userDto);


        ProjectEntity projectEntity = modelMapper.map(newProjectDto, ProjectEntity.class);

        return modelMapper.map(projectRepo.save(projectEntity), ProjectDto.class);
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

        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);

        projectRepo.delete(projectEntity);

    }

}









