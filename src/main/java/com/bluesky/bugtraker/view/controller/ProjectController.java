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
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
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

@Controller
@RequestMapping("/users/{creatorId}/projects")
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


    @PreAuthorize("#creatorId == principal.id")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createProject(@PathVariable String creatorId,
                                           @Valid @ModelAttribute("projectRequestModel")
                                           ProjectRequestModel projectRequestModel) {

        String result = "forms/project-form :: #project-form-block";
        ProjectDto projectDto = modelMapper.map(projectRequestModel, ProjectDto.class);

        projectService.createProject(creatorId, projectDto);

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @PreAuthorize("#creatorId == principal.id or  principal.isSubscribedTo(#creatorId, #projectId)")
    @GetMapping("/{projectId}")
    public String getProject(@PathVariable String creatorId,
                             @PathVariable String projectId,
                             Model model) {

        ProjectDto projectDto = projectService.getProject(projectId);
        ProjectResponseModel projectResponseModel =
                modelMapper.map(projectDto, ProjectResponseModel.class);

        model.addAttribute("project", projectResponseModel);

        WebMvcLinkBuilder baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId);

        model.addAttribute("selfLink", baseLink.toUri().toString());

        String subscribersLink = baseLink.slash("subscribers").slash("/body").toUri().toString();
        model.addAttribute("subscribersLink", subscribersLink);

        String ticketsLink = baseLink.slash("tickets").toUri().toString();
        model.addAttribute("ticketsLink", ticketsLink);

        String commentsLink =
                baseLink.slash("comments").slash("body").toUri().toString();
        model.addAttribute("commentsLink", commentsLink);

        return "pages/project";
    }


    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping()
//    @JsonView(DataTablesOutput.View.class)
    @ResponseBody
    public DataTablesOutput<ProjectResponseModel> getProjects(
            @PathVariable String creatorId,
            @Valid DataTablesInput input) {

        DataTablesOutput<ProjectDto> pagedProjectsDto =
                projectService.getProjects(input);

        DataTablesOutput<ProjectResponseModel> result = new DataTablesOutput<>();
        modelMapper.map(pagedProjectsDto, result);

        result.setData(modelMapper.map(pagedProjectsDto.getData(), new TypeToken<List<ProjectResponseModel>>() {
        }.getType()));

        return result;
    }


    @PreAuthorize("#creatorId == principal.id")
    @PatchMapping("/{projectId}")
    public ProjectResponseModel setProjectName(@PathVariable String creatorId,
                                               @PathVariable String projectId,
                                               @RequestBody ProjectRequestModel projectRequestBody) {

        ProjectDto projectDto = projectService.setProjectName(projectId, projectRequestBody);

        return null;
    }

    @PreAuthorize(value = "#creatorId == principal.id")
    @DeleteMapping(value = "/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String creatorId,
                                           @PathVariable String projectId) {

        projectService.deleteProject(projectId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#creatorId == principal.id")
    @PostMapping("/{projectId}/subscribers")
    public String addSubscriber(@PathVariable String creatorId,
                                @PathVariable String projectId,
                                @ModelAttribute("subscriberRequestModel")
                                SubscriberRequestModel subscriber,
                                BindingResult bindingResult,
                                Model model,
                                HttpServletResponse response) {

        model.addAttribute("projectCreatorId", creatorId);
        model.addAttribute("projectName", projectId);

        try {
            projectService.addSubscriber(projectId, subscriber);
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


    @PreAuthorize("#creatorId == principal.id or principal.isSubscribedTo(#creatorId, #projectId)")
    @GetMapping("/{projectId}/subscribers")
    @ResponseBody
    public DataTablesOutput<UserResponseModel> getSubscribers(
            @PathVariable String creatorId,
            @PathVariable String projectId,
            @Valid DataTablesInput input) {

        DataTablesOutput<UserDto> pagedSubsDtos =
                projectService.getSubscribers(projectId, input);

        DataTablesOutput<UserResponseModel> result = new DataTablesOutput<>();
        modelMapper.map(pagedSubsDtos, result);

        result.setData(modelMapper.map(pagedSubsDtos.getData(), new TypeToken<List<UserResponseModel>>() {
        }.getType()));

        return result;
    }

    @PreAuthorize("#id == principal.id or #subscriberId == principal.id")
    @DeleteMapping("/{projectName}/subscribers/{subscriberId}")
    public HttpEntity<?> removeSubscriber(@PathVariable String creatorId,
                                          @PathVariable String projectName,
                                          @PathVariable String subscriberId) {

        ProjectDto projectDto = projectService.removeSubscriber(
                creatorId, projectName, subscriberId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PreAuthorize("#creatorId == principal.id")
    @PostMapping(value = "/{projectId}/comments",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public HttpEntity<?> createComment(@PathVariable String creatorId,
                                       @PathVariable String projectId,
                                       @AuthenticationPrincipal UserPrincipal creator,
                                       @ModelAttribute("commentForm") CommentRequestModel comment) {

        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);
        projectService.createComment(projectId, creator.getId(), commentDto);

        String commentsList = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("comments")
                .toUri().toString();

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(commentsList);
    }

    @PreAuthorize("#creatorId == principal.id")
    @GetMapping("/{projectName}/comments/body")
    public String getCommentsBody(@PathVariable String creatorId,
                                  @PathVariable String projectName,
                                  Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectName)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("commentsContentLink", baseLink);
        model.addAttribute("commentForm", new CommentRequestModel());
        model.addAttribute("commentPostRequestLink", baseLink);

        return "fragments/comments/comments-body";
    }

    @PreAuthorize("#creatorId == principal.id")
    @GetMapping("/{projectId}/comments")
    public String getComments(@PathVariable String creatorId,
                              @PathVariable String projectId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "limit", defaultValue = "5") int limit,
                              @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
                              @RequestParam(value = "dir", defaultValue = "DESC") Sort.Direction dir,
                              Model model) {

        Page<CommentDto> pagedCommentsDto =
                projectService.getComments(projectId, page, limit, sortBy, dir);

        List<CommentDto> pagedCommentsResponseModel =
                modelMapper.map(pagedCommentsDto.getContent(), new TypeToken<ArrayList<CommentDto>>() {
                }.getType());

        model.addAttribute("limit", limit);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedCommentsDto.getTotalPages());

        model.addAttribute("commentsList", pagedCommentsResponseModel);

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("listRequestLink", baseLink);

        return "fragments/comments/comments-content";
    }


}

















