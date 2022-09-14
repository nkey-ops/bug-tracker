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
@RequestMapping("/users/{userId}/projects/{projectName}/tickets")
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
    public String showTicketForm(@PathVariable String userId,
                                 @PathVariable String projectName,
                                 @ModelAttribute("user") UserResponseModel user,
                                 Model model) {
        model.addAttribute("user", user);

        if (!model.containsAttribute("ticketRequestModel")) {
            model.addAttribute("ticketRequestModel", new TicketRequestModel());
        }

        model.addAttribute("projectCreatorId", userId);
        model.addAttribute("projectName", projectName);

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());

        String ticketFormPostRequestLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("tickets").toUri().toString();

        model.addAttribute("ticketFormPostRequestLink", ticketFormPostRequestLink);

        return "pages/ticket-creation";
    }

    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{ticketId}/edit")
    public String showTicketEditForm(@PathVariable String userId,
                                     @PathVariable String projectName,
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
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("tickets")
                .slash(ticketId)
                .toUri().toString();

        model.addAttribute("ticketEditionPostRequestLink", ticketEditionPostRequestLink);

        return "pages/ticket-edition";
    }


    @PreAuthorize("#userId == principal.id or  principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{ticketId}")
    public String getTicket(@PathVariable String userId,
                            @PathVariable String projectName,
                            @PathVariable String ticketId,
                            Model model) {
        TicketDto ticketDto = ticketService.getTicket(ticketId);

        TicketResponseModel ticketResponseModel = modelMapper.map(ticketDto, TicketResponseModel.class);

        model.addAttribute("ticket", ticketResponseModel);

        WebMvcLinkBuilder baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
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
                baseLink.slash("records").toUri().toString();
        model.addAttribute("ticketRecordsLink", ticketRecordsLink);

        String ticketAssignedDevsLink =
                baseLink.slash("assigned-devs").toUri().toString();
        model.addAttribute("ticketAssignedDevsLink", ticketAssignedDevsLink);

        model.addAttribute("isMainTicket", true);

        return "pages/ticket";
    }

    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping
    public String getTickets(@PathVariable String userId,
                             @PathVariable String projectName,
                             Model model) {

        Set<TicketDto> pagedTicketsDto = ticketService.getTickets(userId, projectName, 5, 10);

        Set<TicketResponseModel> pagedTicketsResponse =
                modelMapper.map(pagedTicketsDto, new TypeToken<Set<TicketResponseModel>>() {
                }.getType());

        model.addAttribute("listName", "Tickets");

        model.addAttribute("elementsList", pagedTicketsResponse);
        model.addAttribute("isEmpty", pagedTicketsResponse.isEmpty());


        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("tickets")
                .toUri().toString();

        model.addAttribute("baseLink", baseLink);

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());


        return "fragments/list/body/tickets-body";
    }


    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createTicket(@PathVariable String userId,
                                          @PathVariable String projectName,
                                          @AuthenticationPrincipal UserPrincipal reporter,
                                          @Valid @ModelAttribute("ticketRequestModel")
                                          TicketRequestModel ticket) {


        TicketDto requestTicketDto = modelMapper.map(ticket, TicketDto.class);

        ticketService.createTicket(userId, projectName, requestTicketDto, reporter.getId());
        
        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .toUri().toString();

        return ResponseEntity.status(HttpStatus.CREATED).body(baseLink);
    }

    @RequestMapping(value = "/{ticketId}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updateTicket(@PathVariable String userId,
                               @PathVariable String projectName,
                               @PathVariable String ticketId,
                               @ModelAttribute("ticketForm") TicketRequestModel ticket,
                               BindingResult bindingResult,
                               RedirectAttributes attr) {
        TicketDto ticketDto = modelMapper.map(ticket, TicketDto.class);

        try {
            ticketService.updateTicket(userId, projectName, ticketId, ticketDto);
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error", e.getErrorType().getErrorMessage()));
            attr.addFlashAttribute("org.springframework.validation.BindingResult.ticketForm", bindingResult);
            attr.addFlashAttribute("ticketForm", ticket);

            return "redirect:/users/" + userId + "/projects/" + projectName + "/bugs/ticket-edit-form";
        }

        return "redirect:/users/" + userId + "/projects/" + projectName + "/tickets/" + ticketId;
    }


    @PreAuthorize("#userId == principal.id")
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<?> deleteTicket(@PathVariable String userId,
                                          @PathVariable String projectName,
                                          @PathVariable String ticketId) {

        ticketService.deleteBug(userId, projectName, ticketId);

        return ResponseEntity.noContent().build();

    }


    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{ticketId}/records")
    public String getTicketRecords(@PathVariable String userId,
                                   @PathVariable String projectName,
                                   @PathVariable String ticketId,
                                   @RequestParam(value = "page", defaultValue = "1") int page,
                                   @RequestParam(value = "limit", defaultValue = "8") int limit,
                                   Model model) {

        Page<TicketRecordDto> ticketRecordsDtos =
                ticketService.getTicketRecords(ticketId, page, limit);

        List<TicketRecordResponseModel> ticketRecordResponseModels =
                modelMapper.map(ticketRecordsDtos.getContent(), new TypeToken<List<TicketRecordResponseModel>>() {
                }.getType());

        model.addAttribute("elementsList", ticketRecordResponseModels);

        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("tickets")
                .slash(ticketId)
                .slash("records")
                .toUri().toString();


        model.addAttribute("baseLink", baseLink);

        return "fragments/list/body/ticket-record-body";
    }

    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{ticketId}/records/{recordId}")
    public String getTicketRecord(@PathVariable String userId,
                                  @PathVariable String projectName,
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

    @PreAuthorize("#userId == principal.id")
    @PostMapping(value = "/{ticketId}/comments",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createComment(@PathVariable String userId,
                                                @PathVariable String projectName,
                                                @PathVariable String ticketId,
                                                @AuthenticationPrincipal UserPrincipal creator,
                                                @ModelAttribute("commentForm") CommentRequestModel comment
    ) {

        CommentDto commentDto = modelMapper.map(comment, CommentDto.class);
        ticketService.createComment(ticketId, creator.getId(), commentDto);
        
        String commentsList = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("tickets")
                .slash(ticketId)
                .slash("comments")
                .toUri().toString();

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(commentsList);
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{ticketId}/comments/body")
    public String getCommentsBody(@PathVariable String userId,
                                  @PathVariable String projectName,
                                  @PathVariable String ticketId, 
                                  Model model){

        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("tickets")
                .slash(ticketId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("commentsContentLink", baseLink);
        model.addAttribute("commentForm", new CommentRequestModel());
        model.addAttribute("commentPostRequestLink", baseLink);
        
         return "fragments/comments/comments-body";
    }
    
    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{ticketId}/comments")
    public String getComments(@PathVariable String userId,
                              @PathVariable String projectName,
                              @PathVariable String ticketId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "limit", defaultValue = "5") int limit,
                              @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
                              @RequestParam(value = "dir", defaultValue = "DESC") Sort.Direction dir,
                              Model model) {

        Page<CommentDto> pagedCommentsDto =
                ticketService.getComments(ticketId, page, limit, sortBy, dir);

        List<CommentResponseModel> pagedCommentsResponseModel = 
                modelMapper.map(pagedCommentsDto.getContent(), new TypeToken<ArrayList<CommentResponseModel>>() {}.getType());

        model.addAttribute("limit", limit);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedCommentsDto.getTotalPages());

        model.addAttribute("commentsList", pagedCommentsResponseModel);

        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("tickets")
                .slash(ticketId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("listRequestLink", baseLink);

        return "fragments/comments/comments-content";
    }


    @PreAuthorize("#userId == principal.id")
    @PostMapping("/{ticketId}/assigned-devs")
    public String addAssignedDev(@PathVariable String userId,
                                 @PathVariable String projectName,
                                 @PathVariable String ticketId,
                                 @ModelAttribute("subscriberRequestModel")
                                 SubscriberRequestModel assignedDev,
                                 BindingResult bindingResult,
                                 HttpServletResponse response) {
        try {
            ticketService.addAssignedDev(ticketId, assignedDev.getPublicId());
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error",
                            e.getErrorType().getErrorMessage()));
        }

        response.setStatus(HttpStatus.CREATED.value());

        return "forms/subscriber-form :: #subscriber-form-block";
    }

    @PreAuthorize("#userId == principal.id  or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{ticketId}/assigned-devs")
    public String getAssignedDevs(
            @PathVariable String userId,
            @PathVariable String projectName,
            @PathVariable String ticketId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "8") int limit,
            Model model) {


        Page<UserDto> assignedDevsDtos = ticketService.getAssignedDevs(ticketId, page, limit);

        Set<UserResponseModel> assignedDevsResponseModel =
                modelMapper.map(assignedDevsDtos.getContent(),
                        new TypeToken<Set<UserResponseModel>>() {
                        }.getType());

        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("tickets")
                .slash(ticketId)
                .slash("assigned-devs")
                .toUri().toString();


        model.addAttribute("listName", "Assigned Developers");
        model.addAttribute("elementsList", assignedDevsResponseModel);
        model.addAttribute("baseLink", baseLink);
        model.addAttribute("subscriberRequestModel", new SubscriberRequestModel());


        return "fragments/list/body/subscribers-body :: #subscribers-body";
    }

    @PreAuthorize("#userId == principal.id or #fixerId == principal.id")
    @DeleteMapping("/{ticketId}/assigned-devs/{assignedDevId}")
    public ResponseEntity<?> removeAssignedDev(@PathVariable String userId,
                                               @PathVariable String projectName,
                                               @PathVariable String ticketId,
                                               @PathVariable String assignedDevId) {

        ticketService.removeAssignedDev(ticketId, assignedDevId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}