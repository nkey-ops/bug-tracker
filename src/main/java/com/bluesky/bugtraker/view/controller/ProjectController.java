package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assembler.ProjectModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assembler.UserModelAssembler;
import com.bluesky.bugtraker.view.model.request.ProjectRequestBody;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users/{userId}/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectModelAssembler modelAssembler;
    private UserModelAssembler userModelAssembler;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ProjectController(ProjectService projectService,
                             ProjectModelAssembler modelAssembler,
                             UserModelAssembler userModelAssembler) {
        this.projectService = projectService;
        this.modelAssembler = modelAssembler;
        this.userModelAssembler = userModelAssembler;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }


    @PreAuthorize("#userId == principal.id")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @PostMapping
    public ProjectResponseModel createProject(@PathVariable String userId,
                                              @RequestBody ProjectRequestBody projectRequestBody) {

        ProjectDto projectDto = projectService.createProject(userId,
                modelMapper.map(projectRequestBody, ProjectDto.class));

        return modelAssembler.toModel(
                modelMapper.map(projectDto, ProjectResponseModel.class));
    }

    @PreAuthorize("#userId == principal.id or  principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{projectName}")
    public ProjectResponseModel getProject(@PathVariable String userId,
                                           @PathVariable String projectName) {
        ProjectDto projectDto = projectService.getProject(userId, projectName);
        ProjectResponseModel responseModel = modelMapper.map(projectDto, ProjectResponseModel.class);

        return modelAssembler.toModel(responseModel);
    }

    @PreAuthorize(value = "#userId == principal.id")
    @GetMapping
    public CollectionModel<ProjectResponseModel> getProjects(@PathVariable String userId,
                                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                                             @RequestParam(value = "limit", defaultValue = "15") int limit) {
        Set<ProjectDto> projectsDto = projectService.getProjects(userId, page, limit);

        Set<ProjectResponseModel> projectResponseModels =
                modelMapper.map(projectsDto,
                        new TypeToken<Set<ProjectResponseModel>>() {
                        }.getType());

        return modelAssembler.toCollectionModelWithSelfRel(projectResponseModels, userId);
    }

    @PreAuthorize("#userId == principal.id")
    @PatchMapping("/{projectName}")
    public ProjectResponseModel setProjectName(@PathVariable String userId,
                                               @PathVariable String projectName,
                                               @RequestBody ProjectRequestBody projectRequestBody) {

        ProjectDto projectDto = projectService.setProjectName(userId, projectName, projectRequestBody);

        return modelAssembler.toModel(
                modelMapper.map(projectDto, ProjectResponseModel.class));
    }

    @PreAuthorize(value = "#userId == principal.id")
    @DeleteMapping(value = "/{projectName}")
    public ResponseEntity<?> deleteProject(@PathVariable String userId,
                                           @PathVariable String projectName) {

        projectService.deleteProject(userId, projectName);

        return ResponseEntity.noContent().build();
    }

    //TODO return response status
    @PutMapping("/{projectName}/subscribers/{subscriberId}")
    public ProjectResponseModel addSubscriber(@PathVariable String userId,
                                              @PathVariable String projectName,
                                              @PathVariable String subscriberId) {

        ProjectDto projectDto = projectService.addSubscriber(
                userId, projectName, subscriberId);


        return modelAssembler.toModel(modelMapper.map(projectDto, ProjectResponseModel.class));
    }
    @GetMapping("/{projectName}/subscribers")
    public CollectionModel<UserResponseModel> getSubscribers(
                                                @PathVariable String userId,
                                                @PathVariable String projectName,
                                                @RequestParam(value = "page", defaultValue = "1") int page,
                                                @RequestParam(value = "limit", defaultValue = "15") int limit) {

        Set<UserDto> userDtos = projectService.getSubscribers(
                userId, projectName, page, limit);

        Set<UserResponseModel> userResponseModels =
                modelMapper.map(userDtos, new TypeToken<Set<UserResponseModel>>() {}.getType());

        return userModelAssembler.toCollectionModel(userResponseModels);
    }
    @DeleteMapping("/{projectName}/subscribers/{subscriberId}")
    public ProjectResponseModel removeSubscriber(@PathVariable String userId,
                                                 @PathVariable String projectName,
                                                 @PathVariable String subscriberId) {

        ProjectDto projectDto = projectService.removeSubscriber(
                userId, projectName, subscriberId);


        return modelAssembler.toModel(modelMapper.map(projectDto, ProjectResponseModel.class));
    }




}

















