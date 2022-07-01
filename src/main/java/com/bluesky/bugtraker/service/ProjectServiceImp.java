package com.bluesky.bugtraker.service;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.entity.UserEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;

@Service
public class ProjectServiceImp implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ProjectServiceImp(ProjectRepository projectRepository,
                             UserService userService,
                             UserService userService1) {
        this.projectRepository = projectRepository;
        this.userService = userService1;
    }

    @Override
    public ProjectDto getProject(String userId, String projectName) {
        UserDto userDto = userService.getUserById(userId);

        return userDto.getUserProjects()
                .stream()
                .filter(project -> project.getName().equals(projectName))
                .findFirst()
                .orElseThrow(() -> new ProjectServiceException(NO_RECORD_FOUND, projectName));
    }

    @Override
    public ProjectDto createProject(String userId, String projectName) {
        UserDto userDto = userService.getUserById(userId);
        ProjectDto projectDto = new ProjectDto();
        projectDto.setName(projectName);
        projectDto.setCreatedBy(userDto);


        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);

        return modelMapper.map(projectRepository.save(projectEntity), ProjectDto.class);
    }

    @Override
    public ProjectDto setProjectName(String userId, String oldName, String newName) {
        ProjectDto projectDto = getProject(userId, oldName);
        projectDto.setName(newName);

        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);

        return modelMapper.map(projectRepository.save(projectEntity), ProjectDto.class);
    }

    @Override
    public void deleteProject(String userId, String projectName) {
        ProjectDto projectDto = getProject(userId, projectName);

        ProjectEntity projectEntity = modelMapper.map(projectDto, ProjectEntity.class);

        projectRepository.delete(projectEntity);

    }

}









