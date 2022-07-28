package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assembler.ProjectModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assembler.UserModelAssembler;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/users/{userId}/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectModelAssembler modelAssembler;
    private  final UserController userController;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ProjectController(ProjectService projectService,
                             ProjectModelAssembler modelAssembler,
                              UserController userController) {
        this.projectService = projectService;
        this.modelAssembler = modelAssembler;
        this.userController = userController;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @ModelAttribute("user")
    public UserResponseModel getCurrentUser(){
        return userController.getCurrentUser();
    }

    @PreAuthorize("#userId == principal.id")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView createProject(@PathVariable String userId,
                                      @ModelAttribute("project")
                                      @Valid ProjectRequestModel projectRequestModel,
                                      @ModelAttribute("user") UserResponseModel user,
                                      BindingResult bindingResult) {

        ModelAndView mav = new ModelAndView("/forms/project-form");
        mav.addObject("user", user);
        if (bindingResult.hasErrors()) return mav;

        ProjectDto projectDto = modelMapper.map(projectRequestModel, ProjectDto.class);

        try {
            projectDto = projectService.createProject(userId, projectDto);
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error", e.getErrorType().getErrorMessage()));
            return mav;
        }

        ProjectResponseModel projectResponseModel =
                modelMapper.map(projectDto, ProjectResponseModel.class);
        projectRequestModel = null;

        return mav.addObject("isCreated", "true");
    }

    @PreAuthorize("#userId == principal.id or  principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{projectName}")
    public String getProject(@PathVariable String userId,
                             @PathVariable String projectName,
                             @ModelAttribute("user") UserResponseModel user,
                             Model model,
                             @RequestParam Map<String, String> params ) {
        ProjectDto projectDto = projectService.getProject(userId, projectName);
        ProjectResponseModel projectResponseModel =
                modelMapper.map(projectDto, ProjectResponseModel.class);

        model.addAttribute("user", user);
        model.addAttribute("project", projectResponseModel);





        return "objects/project";
    }

    @PreAuthorize(value = "#userId == principal.id")
    @GetMapping
    public void getProjects(Model model,
                            @PathVariable String userId,
                            @RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "limit", defaultValue = "15") int limit) {
        Page<ProjectDto> projectsDto = projectService.getProjects(userId, page, limit);

        Page<ProjectResponseModel> projectPages =
                modelMapper.map(projectsDto, new TypeToken<Page<ProjectResponseModel>>() {
                }.getType());


        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", projectPages.getTotalPages());
        model.addAttribute("projectsList", projectPages.getContent());
        model.addAttribute("totalProjects", projectPages.getTotalElements());
    }

    @PreAuthorize("#userId == principal.id")
    @PatchMapping("/{projectName}")
    public ProjectResponseModel setProjectName(@PathVariable String userId,
                                               @PathVariable String projectName,
                                               @RequestBody ProjectRequestModel projectRequestBody) {

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

    @PreAuthorize("#id == principal.id")
    @PutMapping("/{projectName}/subscribers/{subscriberId}")
    public ProjectResponseModel addSubscriber(@PathVariable String userId,
                                              @PathVariable String projectName,
                                              @PathVariable String subscriberId) {

        ProjectDto projectDto = projectService.addSubscriber(
                userId, projectName, subscriberId);


        return modelAssembler.toModel(modelMapper.map(projectDto, ProjectResponseModel.class));
    }

    @PreAuthorize("#id == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{projectName}/subscribers")
    public Set<UserResponseModel> getSubscribers(
            @PathVariable String userId,
            @PathVariable String projectName,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "15") int limit) {

        Set<UserDto> userDtos = projectService.getSubscribers(
                userId, projectName, page, limit);

        Set<UserResponseModel> userResponseModels =
                modelMapper.map(userDtos, new TypeToken<Set<UserResponseModel>>() {
                }.getType());

        return  userResponseModels;
    }

    @PreAuthorize("#id == principal.id or #subscriberId == principal.id")
    @DeleteMapping("/{projectName}/subscribers/{subscriberId}")
    public ProjectResponseModel removeSubscriber(@PathVariable String userId,
                                                 @PathVariable String projectName,
                                                 @PathVariable String subscriberId) {

        ProjectDto projectDto = projectService.removeSubscriber(
                userId, projectName, subscriberId);


        return modelAssembler.toModel(modelMapper.map(projectDto, ProjectResponseModel.class));
    }


}

















