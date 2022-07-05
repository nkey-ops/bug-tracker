package com.bluesky.bugtraker.service.impl;

import com.bluesky.bugtraker.exceptions.serviceexception.ProjectServiceException;
import com.bluesky.bugtraker.io.entity.ProjectEntity;
import com.bluesky.bugtraker.io.repository.ProjectRepository;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.request.ProjectRequestBody;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static com.bluesky.bugtraker.exceptions.ErrorMessages.NO_RECORD_FOUND;
import static com.bluesky.bugtraker.exceptions.ErrorMessages.RECORD_ALREADY_EXISTS;

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


    private Optional<ProjectDto> getProjectOptional(String userId, String projectName){
        UserDto userDto = userService.getUserById(userId);

        return userDto.getUserProjects()
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
    public Set<ProjectDto> getProjects(String userId) {
        UserDto userDto = userService.getUserById(userId);
        return userDto.getUserProjects();

    }

    @Override
    public ProjectDto createProject(String userId, ProjectDto projectDto) {
        if (getProjectOptional(userId, projectDto.getName()).isPresent())
            throw  new ProjectServiceException(RECORD_ALREADY_EXISTS, projectDto.getName());

        UserDto userDto = userService.getUserById(userId);

        ProjectDto newProjectDto = new ProjectDto();
        newProjectDto.setName(projectDto.getName());
        newProjectDto.setCreatedBy(userDto);


        ProjectEntity projectEntity = modelMapper.map(newProjectDto, ProjectEntity.class);

        return modelMapper.map(projectRepository.save(projectEntity), ProjectDto.class);
    }

    @Override
    public ProjectDto setProjectName(String userId, String oldName, ProjectRequestBody projectRequestBody) {
        ProjectDto projectDto = getProject(userId, oldName);
        projectDto.setName(projectRequestBody.getName());

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









