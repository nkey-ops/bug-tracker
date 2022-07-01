package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/{projectName}")
public class ProjectController {
    private final ProjectService projectService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @GetMapping
    public ProjectResponseModel getProject(@PathVariable String userId,
                                           @PathVariable String projectName){
        ProjectDto projectDto = projectService.getProject(userId, projectName);

        return  modelMapper.map(projectDto, ProjectResponseModel.class);
    }

    @PostMapping
    public ProjectResponseModel createProject(@PathVariable String userId,
                                              @PathVariable String projectName){

        ProjectDto projectDto = projectService.createProject(userId, projectName);

        return  modelMapper.map(projectDto, ProjectResponseModel.class);
    }

    @PatchMapping("/{newName}")
    public ProjectResponseModel setProjectName(@PathVariable String userId,
                                               @PathVariable String projectName,
                                               @PathVariable String newName){

        ProjectDto projectDto = projectService.setProjectName(userId, projectName, newName);

        return  modelMapper.map(projectDto, ProjectResponseModel.class);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProject(@PathVariable String userId,
                                           @PathVariable String projectName){

        projectService.deleteProject(userId, projectName);

        return ResponseEntity.noContent().build();
    }


}

















