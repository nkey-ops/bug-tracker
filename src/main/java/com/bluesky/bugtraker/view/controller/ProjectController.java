package com.bluesky.bugtraker.view.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.ProjectService;
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

import jakarta.validation.Valid;

@Controller
@RequestMapping("/users/{creatorId}/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectModelAssembler projectModelAssembler;
    private final UserModelAssembler userModelAssembler;
    private final ModelMapper modelMapper;

    @Autowired
    public ProjectController(ProjectService projectService,
                             ProjectModelAssembler projectModelAssembler,
                             ModelMapper modelMapper,
                             UserModelAssembler userModelAssembler) {
       
        this.projectService = projectService;
        this.projectModelAssembler = projectModelAssembler;
        this.userModelAssembler = userModelAssembler;
        this.modelMapper = modelMapper;
    }


    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                   "#creatorId == principal.id")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createProject(@PathVariable String creatorId,
                                           @Valid ProjectRequestModel projectRequestModel) {

        ProjectDTO projectDto = modelMapper.map(projectRequestModel, ProjectDTO.class);
        projectService.createProject(creatorId, projectDto);

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id or  " +
                  "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{projectId}")
    @ResponseBody
    public ResponseEntity<ProjectResponseModel> getProject(
                                        @PathVariable String creatorId,
                                        @PathVariable String projectId) {

        ProjectDTO projectDto = projectService.getProject(projectId);
        ProjectResponseModel assembledProject = projectModelAssembler.toModel(projectDto);

        return ResponseEntity.ok(assembledProject);

    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id")
    @GetMapping
    @ResponseBody
    public ResponseEntity<DataTablesOutput<ProjectResponseModel>>
                                    getProjects(
                                            @PathVariable String creatorId,
                                            @Valid DataTablesInput input) {

        DataTablesOutput<ProjectDTO> pagedProjectsDto = 
                projectService.getProjects(creatorId, input);

        DataTablesOutput<ProjectResponseModel> assembledProjects = 
                projectModelAssembler.toDataTablesOutputModel(pagedProjectsDto);

        return ResponseEntity.ok(assembledProjects);
    }


    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id")
    @PatchMapping(value = "/{projectId}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> updateProject(@PathVariable String creatorId,
                                           @PathVariable String projectId,
                                           @Valid ProjectRequestModel projectRequestModel) {

        ProjectDTO projectDTO = modelMapper.map(projectRequestModel, ProjectDTO.class);
        projectService.updateProject(projectId, projectDTO);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id")
    @DeleteMapping(value = "/{projectId}")
    public ResponseEntity<?> deleteProject(@PathVariable String creatorId,
                                           @PathVariable String projectId) {

        projectService.deleteProject(projectId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id")
    @PostMapping(value = "/{projectId}/subscribers",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> addSubscriber(@PathVariable String creatorId,
                                            @PathVariable String projectId,
                                            @Valid SubscriberRequestModel subscriber) {

        projectService.addSubscriber(projectId, subscriber);

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }


    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id or " +
                  "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{projectId}/subscribers")
    @ResponseBody
    public ResponseEntity<DataTablesOutput<UserResponseModel>> getSubscribers(
                                                @PathVariable String creatorId,
                                                @PathVariable String projectId,
                                                @Valid DataTablesInput input) {

        DataTablesOutput<UserDTO> pagedSubsDTOs =
                projectService.getSubscribers(projectId, input);

        DataTablesOutput<UserResponseModel> assembledSubscribers = 
                userModelAssembler.toDataTablesOutputModel(pagedSubsDTOs);

        return ResponseEntity.ok(assembledSubscribers);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id or" + 
                  "#subscriberId == principal.id")
    @DeleteMapping("/{projectId}/subscribers/{subscriberId}")
    public ResponseEntity<?> removeSubscriber(@PathVariable String creatorId,
                                              @PathVariable String projectId,
                                              @PathVariable String subscriberId) {
        projectService.removeSubscriber(projectId, subscriberId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id or" +
                  "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @PostMapping(value = "/{projectId}/comments",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createComment(@PathVariable String creatorId,
                                           @PathVariable String projectId,
                                           @AuthenticationPrincipal UserPrincipal creator,
                                           @Valid CommentRequestModel comment) {

        CommentDTO commentDto = modelMapper.map(comment, CommentDTO.class);
        projectService.createComment(projectId, creator.getId(), commentDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PreAuthorize("hasRole('SUPER_ADMIN') or" +
                  "#creatorId == principal.id or " +
                  "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{projectId}/comments")
    public ModelAndView getComments(@PathVariable String creatorId,
                                    @PathVariable String projectId,
                                    @RequestParam(value = "page", defaultValue = "1") int page,
                                    @RequestParam(value = "limit", defaultValue = "5") int limit,
                                    @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
                                    @RequestParam(value = "dir", defaultValue = "DESC") Sort.Direction dir) {

        Page<CommentDTO> pagedCommentsDto =
                projectService.getComments(projectId, page, limit, sortBy, dir);

        List<CommentDTO> pagedCommentsResponseModel = 
                modelMapper.map(pagedCommentsDto.getContent(), new TypeToken<ArrayList<CommentDTO>>() {}.getType());


        ModelAndView model = new ModelAndView("fragments/comments/comments-content");
        model.addObject("totalElements", pagedCommentsDto.getTotalElements());
        model.addObject("limit", limit);
        model.addObject("currentPage", page);
        model.addObject("totalPages", pagedCommentsDto.getTotalPages());

        model.addObject("commentsList", pagedCommentsResponseModel);

        
        String baseLink = linkTo(methodOn(ProjectController.class)
                .createComment(creatorId, projectId, null, null)).toString();
        
        model.addObject("listRequestLink", baseLink);

        return model;
    }


}

