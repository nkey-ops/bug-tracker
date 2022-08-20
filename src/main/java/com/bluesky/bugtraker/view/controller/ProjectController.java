package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assembler.ProjectModelAssembler;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

// TODO change userId to creatorId
@Controller
@RequestMapping("/users/{userId}/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectModelAssembler modelAssembler;
    private final UserController userController;
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
    public UserResponseModel getCurrentUser() {
        return userController.getCurrentUser();
    }


    @PreAuthorize("#userId == principal.id")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createProject(@PathVariable String userId,
                                @Valid @ModelAttribute("projectRequestModel") ProjectRequestModel projectRequestModel,
                                BindingResult bindingResult,
                                Model model) {


        model.addAttribute("creatorId", userId);

        if (bindingResult.hasErrors()) return "forms/project-form";

        ProjectDto projectDto = modelMapper.map(projectRequestModel, ProjectDto.class);

        try {
            projectDto = projectService.createProject(userId, projectDto);
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error", e.getErrorType().getErrorMessage()));
            return "forms/project-form";
        }

        ProjectResponseModel projectResponseModel =
                modelMapper.map(projectDto, ProjectResponseModel.class);

         model.addAttribute("isCreated", "true");
        return "forms/project-form";
    }

    @PreAuthorize("#userId == principal.id or  principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{projectName}")
    public String getProject(@PathVariable String userId,
                             @PathVariable String projectName,
                             @ModelAttribute("user") UserResponseModel user,
                             @RequestParam Map<String, String> params,
                             Model model) {

        ProjectDto projectDto = projectService.getProject(userId, projectName);
        ProjectResponseModel projectResponseModel =
                modelMapper.map(projectDto, ProjectResponseModel.class);

        model.addAttribute("user", user);
        model.addAttribute("project", projectResponseModel);

        String subscribersLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("subscribers").toUri().toString();

        model.addAttribute("subscribersLink", subscribersLink);

        String bugsLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("bugs").toUri().toString();

        model.addAttribute("bugsLink", bugsLink);

        return "pages/project";
    }

    @PreAuthorize(value = "#userId == principal.id")
    @GetMapping
    public String getProjects(
            @PathVariable String userId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "8") int limit,
            @ModelAttribute("user") UserResponseModel user,
            Model model) {
        Set<ProjectDto> projectsDto = projectService.getProjects(userId, page, limit);

        Set<ProjectResponseModel> pagedProjectsResponse =
                modelMapper.map(projectsDto, new TypeToken<Set<ProjectResponseModel>>() {
                }.getType());


        model.addAttribute("listName", "My Projects");

        model.addAttribute("elementsList",  pagedProjectsResponse);
        model.addAttribute("isEmpty",  pagedProjectsResponse.isEmpty());

        model.addAttribute("projectCreatorId", userId);
        model.addAttribute("projectRequestModel", new ProjectRequestModel());


        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .toUri().toString();
        model.addAttribute("baseLink", baseLink);



        return "fragments/list/body/projects-body";
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

    @PreAuthorize("#userId == principal.id")
    @PostMapping("/{projectName}/subscribers")
    public String addSubscriber(@PathVariable String userId,
                                @PathVariable String projectName,
                                @ModelAttribute("subscriberRequestModel")
                                    SubscriberRequestModel subscriber,
                                BindingResult bindingResult,
                                Model model,
                                HttpServletResponse response) {

        model.addAttribute("projectCreatorId", userId);
        model.addAttribute("projectName", projectName);

        try {
            projectService.addSubscriber(
                    userId, projectName, subscriber);
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error",
                            e.getErrorType().getErrorMessage()));
            return "forms/subscriber-form :: subscriber-form";
        }
        response.setStatus(HttpStatus.CREATED.value());

        model.addAttribute("isSuccess", true);
        return "forms/subscriber-form :: #subscriber-form-block";
    }


    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{projectName}/subscribers")
    public String getSubscribers(
            @PathVariable String userId,
            @PathVariable String projectName,
            Model model) {

        Set<UserDto> subsDtos =
                projectService.getSubscribers(
                        userId, projectName);

        Set<UserResponseModel> subscribersResponse =
                modelMapper.map(subsDtos, new TypeToken<Set<UserResponseModel>>() {
                }.getType());

        model.addAttribute("elementsList", subscribersResponse);

        String link = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("subscribers").toUri().toString();

        model.addAttribute("baseLink", link);
        model.addAttribute("subscriberRequestModel", new SubscriberRequestModel());
        model.addAttribute("projectCreatorId", userId);
        model.addAttribute("projectName", projectName);


        return "fragments/list/body/subscribers-body :: #subscribers-body";
    }

    @PreAuthorize("#id == principal.id or #subscriberId == principal.id")
    @DeleteMapping("/{projectName}/subscribers/{subscriberId}")
    public HttpEntity<?> removeSubscriber(@PathVariable String userId,
                                       @PathVariable String projectName,
                                       @PathVariable String subscriberId) {

        ProjectDto projectDto = projectService.removeSubscriber(
                userId, projectName, subscriberId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//        return "redirect:/users/" + userId + "/projects/" + projectName + "/subscribers";
    }


}

















