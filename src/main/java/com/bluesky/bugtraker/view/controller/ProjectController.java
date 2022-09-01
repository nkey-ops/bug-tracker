package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.ProjectDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.CommentRequestModel;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

// TODO change userId to creatorId
@Controller
@RequestMapping("/users/{userId}/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UserController userController;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public ProjectController(ProjectService projectService,
                             UserController userController) {
        this.projectService = projectService;
        this.userController = userController;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @ModelAttribute("user")
    public UserResponseModel getCurrentUser() {
        return userController.getCurrentUser();
    }


    @PreAuthorize("#userId == principal.id")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createProject(@PathVariable String userId,
                                           @Valid @ModelAttribute("projectRequestModel")
                                           ProjectRequestModel projectRequestModel) {

        String result = "forms/project-form :: #project-form-block";
        ProjectDto projectDto = modelMapper.map(projectRequestModel, ProjectDto.class);

        projectService.createProject(userId, projectDto);

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @PreAuthorize("#userId == principal.id or  principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{projectName}")
    public String getProject(@PathVariable String userId,
                             @PathVariable String projectName,
                             Model model) {

        ProjectDto projectDto = projectService.getProject(userId, projectName);
        ProjectResponseModel projectResponseModel =
                modelMapper.map(projectDto, ProjectResponseModel.class);

        model.addAttribute("project", projectResponseModel);

        WebMvcLinkBuilder baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName);

        model.addAttribute("selfLink", baseLink.toUri().toString());

        String subscribersLink = baseLink.slash("subscribers").toUri().toString();
        model.addAttribute("subscribersLink", subscribersLink);

        String ticketsLink = baseLink.slash("tickets").toUri().toString();
        model.addAttribute("ticketsLink", ticketsLink);

        String commentsLink =
                baseLink.slash("comments").slash("body").toUri().toString();
        model.addAttribute("commentsLink", commentsLink);

        return "pages/project";
    }

    @PreAuthorize(value = "#userId == principal.id")
    @GetMapping("/body")
    public String getProjectsBody(@PathVariable String userId,
                                  Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .toUri().toString();

        model.addAttribute("baseLink", baseLink);
        model.addAttribute("projectRequestModel", new ProjectRequestModel());

        return "fragments/list/body/projects-body  :: #my-projects";
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


        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .toUri().toString();

        model.addAttribute("elementsList", pagedProjectsResponse);
        model.addAttribute("baseLink", baseLink);

        return "fragments/list/content/project-content :: #project-content";
    }

    @PreAuthorize("#userId == principal.id")
    @PatchMapping("/{projectName}")
    public ProjectResponseModel setProjectName(@PathVariable String userId,
                                               @PathVariable String projectName,
                                               @RequestBody ProjectRequestModel projectRequestBody) {

        ProjectDto projectDto = projectService.setProjectName(userId, projectName, projectRequestBody);

        return null;
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
            return "forms/subscriber-form :: #subscriber-form-block";
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

        model.addAttribute("listName", "Subscribers");
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
    }

    @PreAuthorize("#userId == principal.id")
    @PostMapping(value = "/{projectName}/comments",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public HttpEntity<?> createComment(@PathVariable String userId,
                                            @PathVariable String projectName,
                                            @AuthenticationPrincipal UserPrincipal creator,
                                            @ModelAttribute("commentForm") CommentRequestModel comment) {

        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);
        projectService.createComment(userId, projectName, creator.getId() , commentDto);

        String commentsList = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("comments")
                .toUri().toString();

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(commentsList);
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{projectName}/comments/body")
    public String getCommentsBody(@PathVariable String userId,
                                  @PathVariable String projectName,
                                  Model model){

        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("commentsContentLink", baseLink);
        model.addAttribute("commentForm", new CommentRequestModel());
        model.addAttribute("commentPostRequestLink", baseLink);

        return "fragments/comments/comments-body";
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{projectName}/comments")
    public String getComments(@PathVariable String userId,
                              @PathVariable String projectName,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "limit", defaultValue = "5") int limit,
                              @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
                              @RequestParam(value = "dir", defaultValue = "DESC") Sort.Direction dir,
                              Model model) {

        Page<CommentDto> pagedCommentsDto =
                projectService.getComments(userId, projectName, page, limit, sortBy, dir);

        List<CommentDto> pagedCommentsResponseModel =
                modelMapper.map(pagedCommentsDto.getContent(), new TypeToken<ArrayList<CommentDto>>() {}.getType());

        model.addAttribute("limit", limit);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedCommentsDto.getTotalPages());

        model.addAttribute("commentsList", pagedCommentsResponseModel);

        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("listRequestLink", baseLink);

        return "fragments/comments/comments-content";
    }


}

















