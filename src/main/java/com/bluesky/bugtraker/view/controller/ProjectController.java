package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.ProjectService;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.dto.CommentDTO;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assambler.ProjectModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assambler.UserModelAssembler;
import com.bluesky.bugtraker.view.model.request.CommentRequestModel;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping("/users/{creatorId}/projects")
public class ProjectController {
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectModelAssembler projectModelAssembler;
    private final UserModelAssembler userModelAssembler;
    private final ModelMapper modelMapper;

    @Autowired
    public ProjectController(ProjectService projectService,
                             UserService userService,
                             ProjectModelAssembler projectModelAssembler,
                             ModelMapper modelMapper,
                             UserModelAssembler userModelAssembler) {
       
        this.projectService = projectService;
        this.userService = userService;
        this.projectModelAssembler = projectModelAssembler;
        this.userModelAssembler = userModelAssembler;
        this.modelMapper = modelMapper;
    }


    @PreAuthorize("#creatorId == principal.id")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createProject(@PathVariable String creatorId,
                                           @Valid @ModelAttribute("projectRequestModel")
                                           ProjectRequestModel projectRequestModel) {

        ProjectDTO projectDto = modelMapper.map(projectRequestModel, ProjectDTO.class);

        projectService.createProject(creatorId, projectDto);

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @PreAuthorize("#creatorId == principal.id or  " +
            "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProject(
            @PathVariable String creatorId,
            @PathVariable String projectId) {

        ProjectDTO projectDto = projectService.getProject(projectId);

        ProjectResponseModel projectResponseModel = 
                modelMapper.map(projectDto, ProjectResponseModel.class);
        ProjectResponseModel assembledProject = 
                projectModelAssembler.toModel(projectResponseModel);

        return ResponseEntity.ok(assembledProject);

    }


    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping
    @ResponseBody
    public ResponseEntity<DataTablesOutput<ProjectResponseModel>> getProjects(
                                                        @PathVariable String creatorId,
                                                        @Valid DataTablesInput input) {

        DataTablesOutput<ProjectDTO> pagedProjectsDto = projectService.getProjects(creatorId, input);

        return ResponseEntity.ok(projectModelAssembler.toDataTablesOutputModel(pagedProjectsDto));
    }


    @PreAuthorize("#creatorId == principal.id")
    @PatchMapping(value = "/{projectId}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> updateProject(@PathVariable String creatorId,
                                           @PathVariable String projectId,
                                           @Valid @ModelAttribute("projectRequestModel")
                                           ProjectRequestModel projectRequestModel) {

        ProjectDTO projectDTO = modelMapper.map(projectRequestModel, ProjectDTO.class);
        projectDTO = projectService.updateProject(projectId, projectDTO);
        
        ProjectResponseModel projectResponseModel = modelMapper.map(projectDTO, ProjectResponseModel.class);

        return ResponseEntity.ok().body(projectResponseModel);
    }

    @PreAuthorize(value = "#creatorId == principal.id")
    @DeleteMapping(value = "/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String creatorId,
                                           @PathVariable String projectId) {

        projectService.deleteProject(projectId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("#creatorId == principal.id")
    @PostMapping(value = "/{projectId}/subscribers",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> addSubscriber(
            @PathVariable String creatorId,
            @PathVariable String projectId,
            @ModelAttribute("subscriberRequestModel")
            SubscriberRequestModel subscriber) {

        projectService.addSubscriber(projectId, subscriber);

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }


    @PreAuthorize("#creatorId == principal.id or " +
                  "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{projectId}/subscribers")
    @ResponseBody
    public ResponseEntity<DataTablesOutput<UserResponseModel>> getSubscribers(
                                                @PathVariable String creatorId,
                                                @PathVariable String projectId,
                                                @Valid DataTablesInput input) {

        DataTablesOutput<UserDTO> pagedSubsDtos =
                projectService.getSubscribers(projectId, input);

        return ResponseEntity.ok(userModelAssembler.toDataTablesOutputModel(pagedSubsDtos));
    }

    @PreAuthorize("#creatorId == principal.id or #subscriberId == principal.id")
    @DeleteMapping("/{projectId}/subscribers/{subscriberId}")
    public ResponseEntity<?> removeSubscriber(@PathVariable String creatorId,
                                              @PathVariable String projectId,
                                              @PathVariable String subscriberId) {
        projectService.removeSubscriber(projectId, subscriberId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PreAuthorize("#creatorId == principal.id or " +
            "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @PostMapping(value = "/{projectId}/comments",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createComment(@PathVariable String creatorId,
                                           @PathVariable String projectId,
                                           @AuthenticationPrincipal UserPrincipal creator,
                                           @ModelAttribute("commentForm") CommentRequestModel comment) {

        CommentDTO commentDto = modelMapper.map(comment, CommentDTO.class);
        projectService.createComment(projectId, creator.getId(), commentDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PreAuthorize("#creatorId == principal.id or " +
              "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{projectId}/comments")
    public String getComments(@PathVariable String creatorId,
                              @PathVariable String projectId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "limit", defaultValue = "5") int limit,
                              @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
                              @RequestParam(value = "dir", defaultValue = "DESC") Sort.Direction dir,
                              Model model) {

        Page<CommentDTO> pagedCommentsDto =
                projectService.getComments(projectId, page, limit, sortBy, dir);

        List<CommentDTO> pagedCommentsResponseModel = modelMapper.map(pagedCommentsDto.getContent(), new TypeToken<ArrayList<CommentDTO>>() {}.getType());

        model.addAttribute("totalElements", pagedCommentsDto.getTotalElements());
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

















