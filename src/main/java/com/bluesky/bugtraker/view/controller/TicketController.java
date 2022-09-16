package com.bluesky.bugtraker.view.controller;


import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.TicketRecordDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.model.rensponse.CommentResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketRecordResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.CommentRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import com.bluesky.bugtraker.view.model.request.TicketRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping("/users/{creatorId}/projects/{projectId}/tickets")
public class TicketController {

    private final UserController userController;
    private final TicketService ticketService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public TicketController(UserController userController, TicketService ticketService) {
        this.userController = userController;
        this.ticketService = ticketService;

        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    }

    @ModelAttribute("user")
    public UserResponseModel getCurrentUser() {
        return userController.getCurrentUser();
    }


    @GetMapping("/ticket-form")
    public String showTicketForm(@PathVariable String creatorId,
                                 @PathVariable String projectId,
                                 @ModelAttribute("user") UserResponseModel user,
                                 Model model) {
        model.addAttribute("user", user);

        if (!model.containsAttribute("ticketRequestModel")) {
            model.addAttribute("ticketRequestModel", new TicketRequestModel());
        }

        model.addAttribute("projectCreatorId", creatorId);
        model.addAttribute("projectName", projectId);

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());

        String ticketFormPostRequestLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("tickets").toUri().toString();

        model.addAttribute("ticketFormPostRequestLink", ticketFormPostRequestLink);

        return "pages/ticket-creation";
    }

    @PreAuthorize("#creatorId == principal.id or principal.isSubscribedTo(#creatorId, #projectId)")
    @GetMapping("/{ticketId}/edit")
    public String showTicketEditForm(@PathVariable String creatorId,
                                     @PathVariable String projectId,
                                     @PathVariable String ticketId,
                                     @ModelAttribute("user") UserResponseModel user,
                                     Model model) {
        TicketDto ticketDto = ticketService.getTicket(ticketId);
        TicketResponseModel ticketResponseModel = modelMapper.map(ticketDto, TicketResponseModel.class);

        model.addAttribute("user", user);

        if (!model.containsAttribute("ticketForm")) {
            model.addAttribute("ticketForm", new TicketRequestModel());
        }

        model.addAttribute("ticket", ticketResponseModel);

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());

        String ticketEditionPostRequestLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("tickets")
                .slash(ticketId)
                .toUri().toString();

        model.addAttribute("ticketEditionPostRequestLink", ticketEditionPostRequestLink);

        return "pages/ticket-edition";
    }


    @PreAuthorize("#creatorId == principal.id or  principal.isSubscribedTo(#creatorId, #projectId)")
    @GetMapping("/{ticketId}")
    public String getTicket(@PathVariable String creatorId,
                            @PathVariable String projectId,
                            @PathVariable String ticketId,
                            Model model) {
        TicketDto ticketDto = ticketService.getTicket(ticketId);

        TicketResponseModel ticketResponseModel = modelMapper.map(ticketDto, TicketResponseModel.class);

        model.addAttribute("ticket", ticketResponseModel);

        WebMvcLinkBuilder baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("tickets")
                .slash(ticketId);

        String ticketEditForm =
                baseLink.slash("edit").toUri().toString();
        model.addAttribute("ticketEditFormLink", ticketEditForm);

        String ticketCommentsLink =
                baseLink.slash("comments")
                        .slash("body").toUri().toString();
        model.addAttribute("ticketCommentsLink", ticketCommentsLink);

        String ticketRecordsLink =
                baseLink.slash("records").slash("/body").toUri().toString();
        model.addAttribute("ticketRecordsLink", ticketRecordsLink);

        String ticketAssignedDevsLink =
                baseLink.slash("assigned-devs").slash("/body").toUri().toString();
        model.addAttribute("ticketAssignedDevsLink", ticketAssignedDevsLink);

        model.addAttribute("isMainTicket", true);

        return "pages/ticket";
    }

    @PreAuthorize("#creatorId == principal.id or principal.isSubscribedTo(#creatorId, #projectId)")
    @GetMapping
    @ResponseBody
    public DataTablesOutput<TicketResponseModel> getTickets(
            @PathVariable String creatorId,
            @PathVariable String projectId,
            @Valid DataTablesInput input) {

        DataTablesOutput<TicketDto> pagedTicketsDtos =
                ticketService.getTickets(projectId, input);


        DataTablesOutput<TicketResponseModel> result = new DataTablesOutput<>();
        modelMapper.map(pagedTicketsDtos, result);
        result.setData(modelMapper.map(pagedTicketsDtos.getData(), new TypeToken<List<TicketResponseModel>>() {
        }.getType()));

        return result;
    }


    @PreAuthorize("#creatorId == principal.id or principal.isSubscribedTo(#creatorId, #projectId)")
    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createTicket(@PathVariable String creatorId,
                                          @PathVariable String projectId,
                                          @AuthenticationPrincipal UserPrincipal reporter,
                                          @Valid @ModelAttribute("ticketRequestModel")
                                          TicketRequestModel ticket) {


        TicketDto requestTicketDto = modelMapper.map(ticket, TicketDto.class);

        ticketService.createTicket(projectId, requestTicketDto, reporter.getId());

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .toUri().toString();

        return ResponseEntity.status(HttpStatus.CREATED).body(baseLink);
    }

    @RequestMapping(value = "/{ticketId}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updateTicket(@PathVariable String creatorId,
                               @PathVariable String projectId,
                               @PathVariable String ticketId,
                               @ModelAttribute("ticketForm") TicketRequestModel ticket,
                               BindingResult bindingResult,
                               RedirectAttributes attr,
                               @AuthenticationPrincipal UserPrincipal reporter) {
        TicketDto ticketDto = modelMapper.map(ticket, TicketDto.class);

        try {
            ticketService.updateTicket(ticketId, ticketDto, reporter.getId());
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error", e.getErrorType().getErrorMessage()));
            attr.addFlashAttribute("org.springframework.validation.BindingResult.ticketForm", bindingResult);
            attr.addFlashAttribute("ticketForm", ticket);

            return "redirect:/users/" + creatorId + "/projects/" + projectId + "/bugs/ticket-edit-form";
        }

        return "redirect:/users/" + creatorId + "/projects/" + projectId + "/tickets/" + ticketId;
    }


    @PreAuthorize("#creatorId == principal.id")
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> deleteTicket(@PathVariable String creatorId,
                                          @PathVariable String projectId,
                                          @PathVariable String ticketId) {

        ticketService.deleteBug(creatorId, projectId, ticketId);

        return ResponseEntity.noContent().build();

    }


    @PreAuthorize("#creatorId == principal.id or principal.isSubscribedTo(#creatorId, #projectId)")
    @GetMapping("/{ticketId}/records")
    @ResponseBody
    public DataTablesOutput<TicketRecordResponseModel> getTicketRecords(@PathVariable String creatorId,
                                                                        @PathVariable String projectId,
                                                                        @PathVariable String ticketId,
                                                                        DataTablesInput input) {

        DataTablesOutput<TicketRecordDto> ticketRecordsDtos =
                ticketService.getTicketRecords(ticketId, input);

        DataTablesOutput<TicketRecordResponseModel> result = new DataTablesOutput<>();
        modelMapper.map(ticketRecordsDtos, result);
        result.setData(modelMapper.map(ticketRecordsDtos.getData(), new TypeToken<List<TicketRecordResponseModel>>() {
        }.getType()));

        return result;
    }

    @PreAuthorize("#creatorId == principal.id or principal.isSubscribedTo(#creatorId, #projectId)")
    @GetMapping("/{ticketId}/records/{recordId}")
    public String getTicketRecord(@PathVariable String creatorId,
                                  @PathVariable String projectId,
                                  @PathVariable String ticketId,
                                  @PathVariable String recordId,
                                  Model model) {

        TicketRecordDto ticketRecord =
                ticketService.getTicketRecord(recordId);

        TicketRecordResponseModel ticketRecordResponseModel =
                modelMapper.map(ticketRecord, TicketRecordResponseModel.class);

        model.addAttribute("ticket", ticketRecordResponseModel);
        model.addAttribute("isMainTicket", false);
        return "details/ticket-details :: ticket-details";
    }

    @PreAuthorize("#creatorId == principal.id")
    @PostMapping(value = "/{ticketId}/comments",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createComment(@PathVariable String creatorId,
                                           @PathVariable String projectId,
                                           @PathVariable String ticketId,
                                           @AuthenticationPrincipal UserPrincipal creator,
                                           @ModelAttribute("commentForm") CommentRequestModel comment
    ) {

        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);
        ticketService.createComment(ticketId, creator.getId(), commentDto);

        String commentsList = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("tickets")
                .slash(ticketId)
                .slash("comments")
                .toUri().toString();

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(commentsList);
    }

    @PreAuthorize("#creatorId == principal.id")
    @GetMapping("/{ticketId}/comments/body")
    public String getCommentsBody(@PathVariable String creatorId,
                                  @PathVariable String projectId,
                                  @PathVariable String ticketId,
                                  Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("tickets")
                .slash(ticketId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("commentsContentLink", baseLink);
        model.addAttribute("commentForm", new CommentRequestModel());
        model.addAttribute("commentPostRequestLink", baseLink);

        return "fragments/comments/comments-body";
    }

    @PreAuthorize("#creatorId == principal.id")
    @GetMapping("/{ticketId}/comments")
    public String getComments(@PathVariable String creatorId,
                              @PathVariable String projectId,
                              @PathVariable String ticketId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "limit", defaultValue = "5") int limit,
                              @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
                              @RequestParam(value = "dir", defaultValue = "DESC") Sort.Direction dir,
                              Model model) {

        Page<CommentDto> pagedCommentsDto =
                ticketService.getComments(ticketId, page, limit, sortBy, dir);

        List<CommentResponseModel> pagedCommentsResponseModel =
                modelMapper.map(pagedCommentsDto.getContent(), new TypeToken<ArrayList<CommentResponseModel>>() {
                }.getType());

        model.addAttribute("limit", limit);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedCommentsDto.getTotalPages());

        model.addAttribute("commentsList", pagedCommentsResponseModel);

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("tickets")
                .slash(ticketId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("listRequestLink", baseLink);

        return "fragments/comments/comments-content";
    }

    @PreAuthorize("#creatorId == principal.id")
    @PostMapping(value = "/{ticketId}/assigned-devs",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> addAssignedDev(
            @PathVariable String creatorId,
            @PathVariable String projectId,
            @PathVariable String ticketId,
            @ModelAttribute("subscriberRequestModel")
            SubscriberRequestModel subscriber ) {

        ticketService.addAssignedDev(ticketId, subscriber.getPublicId());

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }


    @PreAuthorize("#creatorId == principal.id or principal.isSubscribedTo(#creatorId, #projectId)")
    @GetMapping("/{ticketId}/assigned-devs")
    @ResponseBody
    public DataTablesOutput<UserResponseModel> getAssignedDevs(
            @PathVariable String creatorId,
            @PathVariable String projectId,
            @PathVariable String ticketId,
            @Valid DataTablesInput input) {

        DataTablesOutput<UserDto> pagedAssignedDevsDto =
                ticketService.getAssignedDevs(ticketId, input);


        DataTablesOutput<UserResponseModel> result = new DataTablesOutput<>();
        modelMapper.map(pagedAssignedDevsDto, result);
        result.setData(modelMapper.map(pagedAssignedDevsDto.getData(), new TypeToken<List<UserResponseModel>>() {
        }.getType()));

        return result;
    }

    @PreAuthorize("#creatorId == principal.id or #subscriberId == principal.id")
    @DeleteMapping("/{ticketId}/assigned-devs/{assignedDevId}")
    public ResponseEntity<?> removeAssignedDev(@PathVariable String creatorId,
                                              @PathVariable String projectId,
                                              @PathVariable String ticketId,
                                              @PathVariable String assignedDevId) {

        ticketService.removeAssignedDev(ticketId, assignedDevId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}