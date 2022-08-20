package com.bluesky.bugtraker.view.controller;


import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.shared.dto.CommentDto;
import com.bluesky.bugtraker.shared.dto.TicketDto;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.model.rensponse.CommentResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assembler.BugModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assembler.UserModelAssembler;
import com.bluesky.bugtraker.view.model.request.CommentRequestModel;
import com.bluesky.bugtraker.view.model.request.TicketRequestModel;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/users/{userId}/projects/{projectName}/bugs")
public class TicketController {

    private final UserController userController;
    private final TicketService ticketService;
    private final BugModelAssembler modelAssembler;
    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public TicketController(UserController userController, TicketService ticketService, BugModelAssembler modelAssembler) {
        this.userController = userController;
        this.ticketService = ticketService;
        this.modelAssembler = modelAssembler;

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
        model.addAttribute("ticketRequestModel", new TicketRequestModel());

        model.addAttribute("projectCreatorId", userId);
        model.addAttribute("projectName", projectName);

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());

        return "pages/ticket-creation";
    }



    @PreAuthorize("#userId == principal.id or  principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{bugId}")
    public String getTicket(@PathVariable String userId,
                            @PathVariable String projectName,
                            @PathVariable String bugId,
                            Model model) {
        TicketDto ticketDto = ticketService.getTicket(userId, projectName, bugId);

        TicketResponseModel ticketResponseModel = modelMapper.map(ticketDto, TicketResponseModel.class);

        model.addAttribute("ticket", ticketResponseModel);

        String ticketEditForm = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("bugs")
                .slash(bugId)
                .slash("edit")
                .toUri().toString();

        model.addAttribute("ticketEditFormLink", ticketEditForm);

        String ticketCommentsLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("bugs")
                .slash(bugId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("ticketCommentsLink", ticketCommentsLink);

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
                .slash("bugs")
                .toUri().toString();

        model.addAttribute("baseLink", baseLink);

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());


        return "fragments/list/body/tickets";
    }


    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")
    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String createTicket(@PathVariable String userId,
                               @PathVariable String projectName,
                               @AuthenticationPrincipal UserPrincipal reporter,
                               @ModelAttribute("ticketRequestModel") TicketRequestModel ticket,
                               BindingResult bindingResult,
                               HttpServletResponse response) {

        TicketDto requestTicketDto = modelMapper.map(ticket, TicketDto.class);

        try {
            ticketService.createTicket(userId, projectName, requestTicketDto, reporter.getId());
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error", e.getErrorType().getErrorMessage()));

//            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "fragments/error-message";
        }


        return "redirect:/users/" + userId + "/projects/" + projectName;
    }

    @PreAuthorize("#userId == principal.id or principal.isSubscribedTo(#userId, #projectName)")

    @GetMapping("/{bugId}/edit")
    public String showTicketEditForm(@PathVariable String userId,
                                     @PathVariable String projectName,
                                     @PathVariable String bugId,
                                     @ModelAttribute("user") UserResponseModel user,
                                     Model model) {

        TicketDto ticketDto = ticketService.getTicket(userId, projectName, bugId);
        TicketResponseModel ticketResponseModel = modelMapper.map(ticketDto, TicketResponseModel.class);

        model.addAttribute("user", user);

        model.addAttribute("ticketForm", new TicketRequestModel());
        model.addAttribute("ticket", ticketResponseModel);

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());

        return "pages/ticket-edition";
    }

    @RequestMapping(value = "/{bugId}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updateTicket(@PathVariable String userId,
                               @PathVariable String projectName,
                               @PathVariable String bugId,
                               @ModelAttribute("ticketForm") TicketRequestModel bug,
                               BindingResult bindingResult) {

        try {
            ticketService.updateBug(userId, projectName, bugId, modelMapper.map(bug, TicketDto.class));
        } catch (ServiceException e) {
            bindingResult.addError(new ObjectError("error", e.getErrorType().getErrorMessage()));
            return "pages/ticket-edition :: ticket-edition";
        }

        return "redirect:/users/" + userId + "/projects/" + projectName + "/bugs/" + bugId;
    }




    @PreAuthorize("#userId == principal.id")
    @DeleteMapping("/{bugId}")
    public ResponseEntity<?> deleteBug(@PathVariable String userId,
                                       @PathVariable String projectName,
                                       @PathVariable String bugId) {

        ticketService.deleteBug(userId, projectName, bugId);

        return ResponseEntity.noContent().build();

    }

    @PreAuthorize("#userId == principal.id")
    @PostMapping(value = "/{bugId}/comments",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public String createComment(@PathVariable String userId,
                                @PathVariable String projectName,
                                @PathVariable String bugId,
                                @ModelAttribute("commentForm") CommentRequestModel comment,
                                BindingResult bindingResult,
                                Model model) {

        try {
            ticketService.createComment(
                    userId, projectName, bugId,
                    modelMapper.map(comment, CommentDto.class)
            );
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error", e.getErrorType().getErrorMessage()));
        }

        return "redirect:/users/" + userId + "/projects/" + projectName + "/bugs/" + bugId + "/comments";
    }

    @PreAuthorize("#userId == principal.id")
    @GetMapping("/{bugId}/comments")
    public String getComments(@PathVariable String userId,
                              @PathVariable String projectName,
                              @PathVariable String bugId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "limit", defaultValue = "5") int limit,
                              @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
                              @RequestParam(value = "dir", defaultValue = "desc") String order,
                              Model model) {

        Page<CommentDto> pagedCommentsDto =
                ticketService.getComments(userId, projectName, bugId, page, limit, sortBy, order);

        List<CommentResponseModel> pagedCommentsResponseModel =
                modelMapper.map(pagedCommentsDto.getContent(),
                        new TypeToken<ArrayList<CommentResponseModel>>() {
                        }.getType());

        model.addAttribute("limit", limit);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedCommentsDto.getTotalPages());


        model.addAttribute("commentsList", pagedCommentsResponseModel);
        model.addAttribute("commentForm", new CommentRequestModel());

        String baseLink = linkTo(UserController.class)
                .slash(userId)
                .slash("projects")
                .slash(projectName)
                .slash("bugs")
                .slash(bugId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("baseLink", baseLink);


        return "fragments/comments/ticket-comments :: #comments-block";
    }


    @PreAuthorize("#userId == principal.id")
    @PutMapping("/{bugId}/fixers/{fixerId}")
    public ResponseEntity<?> addBugFixer(@PathVariable String userId,
                                         @PathVariable String projectName,
                                         @PathVariable String bugId,
                                         @PathVariable String fixerId) {

        ticketService.addBugFixer(userId, projectName, bugId, fixerId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#userId == principal.id or #fixerId == principal.id")
    @DeleteMapping("/{bugId}/fixers/{fixerId}")
    public ResponseEntity<?> removeBugFixer(@PathVariable String userId,
                                            @PathVariable String projectName,
                                            @PathVariable String bugId,
                                            @PathVariable String fixerId) {

        ticketService.removeBugFixer(userId, projectName, bugId, fixerId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("#userId == principal.id  or principal.isSubscribedTo(#userId, #projectName)")
    @GetMapping("/{bugId}/fixers")
    public CollectionModel<UserResponseModel> getBugFixers(
            @PathVariable String userId,
            @PathVariable String projectName,
            @PathVariable String bugId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "8") int limit) {


        Set<UserDto> bugFixers = ticketService.getBugFixers(userId, projectName, bugId, page, limit);

        Set<UserResponseModel> bugFixerResponse =
                modelMapper.map(bugFixers, new TypeToken<Set<UserResponseModel>>() {
                }.getType());

        return new UserModelAssembler().
                toCollectionModel(bugFixerResponse).
                add(linkTo(methodOn(TicketController.class).
                        getBugFixers(userId, projectName, bugId, page, limit)).withSelfRel());

    }
}