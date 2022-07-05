package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.request.ProjectRequestBody;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users/{userId}/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @GetMapping("/{projectName}")
    public ProjectResponseModel getProject(@PathVariable String userId,
                                               @PathVariable String projectName){
        ProjectDto projectDto = projectService.getProject(userId, projectName);

        return  modelMapper.map(projectDto, ProjectResponseModel.class);
    }

    @GetMapping
    public Set<ProjectResponseModel> getProjects(@PathVariable String userId){
        Set<ProjectDto> projectsDto = projectService.getProjects(userId);

        return  modelMapper.map(projectsDto, new TypeToken<Set<ProjectResponseModel>>() {}.getType());
    }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectResponseModel createProject(@PathVariable String userId,
                                               @RequestBody ProjectRequestBody projectRequestBody){

        ProjectDto projectDto = projectService.createProject(userId, modelMapper.map(projectRequestBody, ProjectDto.class));

        return  modelMapper.map(projectDto, ProjectResponseModel.class);
    }

    @PatchMapping(value = "/{projectName}",
                  consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProjectResponseModel setProjectName(@PathVariable String userId,
                                               @PathVariable String projectName,
                                               @RequestBody ProjectRequestBody projectRequestBody){


        ProjectDto projectDto = projectService.setProjectName(userId, projectName, projectRequestBody);

        return  modelMapper.map(projectDto, ProjectResponseModel.class);
    }

    @DeleteMapping(value = "/{projectName}")
    public ResponseEntity<?> deleteProject(@PathVariable String userId,
                                           @PathVariable String projectName){

        projectService.deleteProject(userId, projectName);

        return ResponseEntity.noContent().build();
    }


}

















