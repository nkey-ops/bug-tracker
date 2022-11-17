package com.bluesky.bugtraker.view.controller;


import com.bluesky.bugtraker.exceptions.serviceexception.ServiceException;
import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.shared.dto.CommentDTO;
import com.bluesky.bugtraker.shared.dto.TicketDTO;
import com.bluesky.bugtraker.shared.dto.TicketRecordDTO;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.model.rensponse.CommentResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketRecordResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.assambler.TicketModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assambler.TicketRecordModelAssembler;
import com.bluesky.bugtraker.view.model.rensponse.assambler.UserModelAssembler;
import com.bluesky.bugtraker.view.model.request.CommentRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import com.bluesky.bugtraker.view.model.request.TicketRequestModel;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping("/users/{creatorId}/projects/{projectId}/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final TicketModelAssembler ticketModelAssembler;
    private final TicketRecordModelAssembler ticketRecordModelAssembler;
    private final UserModelAssembler userModelAssembler;
    private final ModelMapper modelMapper;

    @Autowired
    public TicketController(TicketService ticketService,
                            TicketModelAssembler ticketModelAssembler,
                            TicketRecordModelAssembler ticketRecordModelAssembler, 
                            UserModelAssembler userModelAssembler,
                            ModelMapper modelMapper) {

        this.ticketService = ticketService;
        this.ticketModelAssembler = ticketModelAssembler;
        this.ticketRecordModelAssembler = ticketRecordModelAssembler;
        this.userModelAssembler = userModelAssembler;
        this.modelMapper = modelMapper;
    }


    @PreAuthorize("#creatorId == principal.id or " +
                  "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponseModel> getTicket(
                                                        @PathVariable String creatorId,
                                                        @PathVariable String projectId,
                                                        @PathVariable String ticketId) {

        TicketDTO ticketDto = ticketService.getTicket(ticketId);
        TicketResponseModel ticketResponseModel = modelMapper.map(ticketDto, TicketResponseModel.class);

        return ResponseEntity.ok(ticketModelAssembler.toModel(ticketResponseModel));
    }

    @PreAuthorize("#creatorId == principal.id or @userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping
    @ResponseBody
    public ResponseEntity<DataTablesOutput<TicketResponseModel>> getTickets(
            @PathVariable String creatorId,
            @PathVariable String projectId,
            @Valid DataTablesInput input) {

        DataTablesOutput<TicketDTO> pagedTicketsDtos =
                ticketService.getTickets(projectId, input);

        DataTablesOutput<TicketResponseModel> pagedTickets =
                ticketModelAssembler.toDataTablesOutputModel(pagedTicketsDtos);

        return ResponseEntity.ok(pagedTickets);
    }


    @PreAuthorize("#creatorId == principal.id or @userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> createTicket(@PathVariable String creatorId,
                                          @PathVariable String projectId,
                                          @AuthenticationPrincipal UserPrincipal reporter,
                                          @Valid @ModelAttribute("ticketRequestModel")
                                          TicketRequestModel ticket) {


        TicketDTO requestTicketDTO = modelMapper.map(ticket, TicketDTO.class);

        ticketService.createTicket(projectId, requestTicketDTO, reporter.getId());

        String projectPageLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("/page")
                .toUri().toString();

        return ResponseEntity.status(HttpStatus.CREATED).body(projectPageLink);
    }

    @PreAuthorize("#creatorId == principal.id or " +
                    "@userServiceImp.isSubscribedToTicket(principal.id, #creatorId)")
    @RequestMapping(value = "/{ticketId}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updateTicket(@PathVariable String creatorId,
                               @PathVariable String projectId,
                               @PathVariable String ticketId,
                               @ModelAttribute("ticketForm") TicketRequestModel ticket,
                               BindingResult bindingResult,
                               RedirectAttributes attr,
                               @AuthenticationPrincipal UserPrincipal reporter) {
        TicketDTO ticketDto = modelMapper.map(ticket, TicketDTO.class);

        try {
            ticketService.updateTicket(ticketId, ticketDto, reporter.getId());
        } catch (ServiceException e) {
            bindingResult.addError(
                    new ObjectError("error", e.getErrorType().getText()));
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

        ticketService.deleteTicket(ticketId);

        return ResponseEntity.noContent().build();

    }


    @PreAuthorize("#creatorId == principal.id or @userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{ticketId}/records")
    @ResponseBody
    public ResponseEntity<DataTablesOutput<TicketRecordResponseModel>> getTicketRecords(@PathVariable String creatorId,
                                                                                        @PathVariable String projectId,
                                                                                        @PathVariable String ticketId,
                                                                                        DataTablesInput input) {
        DataTablesOutput<TicketRecordDTO> ticketRecordsDTOs =
                ticketService.getTicketRecords(ticketId, input);

        DataTablesOutput<TicketRecordResponseModel> ticketRecords =
                ticketRecordModelAssembler.toDataTablesOutputModel(ticketRecordsDTOs);

        return ResponseEntity.ok(ticketRecords);
    }

    @PreAuthorize("#creatorId == principal.id or @userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{ticketId}/records/{recordId}")
    public ResponseEntity<TicketRecordResponseModel> getTicketRecord(@PathVariable String creatorId,
                                                                     @PathVariable String projectId,
                                                                     @PathVariable String ticketId,
                                                                     @PathVariable String recordId) {

        TicketRecordDTO ticketRecord =
                ticketService.getTicketRecord(recordId);

        TicketRecordResponseModel ticketRecordResponseModel =
                modelMapper.map(ticketRecord, TicketRecordResponseModel.class);

        TicketRecordResponseModel assembledTicketRecord = 
                ticketRecordModelAssembler.toModel(ticketRecordResponseModel);

        return ResponseEntity.ok(assembledTicketRecord);
    }

    @PreAuthorize("#creatorId == principal.id or @userServiceImp.isSubscribedToTicket(principal.id, #ticketId)")
    @PostMapping(value = "/{ticketId}/comments",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public ResponseEntity<?> createComment(@PathVariable String creatorId,
                                           @PathVariable String projectId,
                                           @PathVariable String ticketId,
                                           @AuthenticationPrincipal UserPrincipal creator,
                                           @ModelAttribute("commentForm") CommentRequestModel comment) {

        CommentDTO commentDto = modelMapper.map(comment, CommentDTO.class);
        ticketService.createComment(ticketId, creator.getId(), commentDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("@ticketAccessEvaluator.areCommentsAllowed(principal.id, #creatorId, #ticketId)")
    @GetMapping("/{ticketId}/comments")
    public String getComments(@PathVariable String creatorId,
                              @PathVariable String projectId,
                              @PathVariable String ticketId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "limit", defaultValue = "5") int limit,
                              @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
                              @RequestParam(value = "dir", defaultValue = "DESC") Sort.Direction dir,
                              Model model) {

        Page<CommentDTO> pagedCommentsDto =
                ticketService.getComments(ticketId, page, limit, sortBy, dir);

        List<CommentResponseModel> pagedCommentsResponseModel =
                modelMapper.map(pagedCommentsDto.getContent(), new TypeToken<ArrayList<CommentResponseModel>>() {
                }.getType());

        model.addAttribute("limit", limit);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagedCommentsDto.getTotalPages());
        model.addAttribute("totalElements", pagedCommentsDto.getTotalElements());
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
    @PostMapping(value = "/{ticketId}/subscribers",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> addSubscriber(
            @PathVariable String creatorId,
            @PathVariable String projectId,
            @PathVariable String ticketId,
            @ModelAttribute("subscriberRequestModel")
            SubscriberRequestModel subscriber) {

        ticketService.addSubscriber(ticketId, subscriber.getPublicId());

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }


    @PreAuthorize("#creatorId == principal.id or @userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{ticketId}/subscribers")
    @ResponseBody
    public ResponseEntity<DataTablesOutput<UserResponseModel>> getSubscribers(
            @PathVariable String creatorId,
            @PathVariable String projectId,
            @PathVariable String ticketId,
            @Valid DataTablesInput input) {

        DataTablesOutput<UserDTO> pagedSubscribersDto =
                ticketService.getSubscribers(ticketId, input);
        DataTablesOutput<UserResponseModel> assembledSubscribers = 
                userModelAssembler.toDataTablesOutputModel(pagedSubscribersDto);
       
        return ResponseEntity.ok(assembledSubscribers);
    }

    @PreAuthorize("#creatorId == principal.id or #subscriberId == principal.id")
    @DeleteMapping("/{ticketId}/subscribers/{subscriberId}")
    public ResponseEntity<?> removeSubscriber(@PathVariable String creatorId,
                                              @PathVariable String projectId,
                                              @PathVariable String ticketId,
                                              @PathVariable String subscriberId) {

        ticketService.removeSubscriber(ticketId, subscriberId);

        return ResponseEntity.ok().build();
    }
}