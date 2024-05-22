package com.bluesky.bugtraker.view.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
import jakarta.validation.Valid;
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

@Controller
@RequestMapping("/users/{creatorId}/projects/{projectId}/tickets")
public class TicketController {
  private final TicketService ticketService;
  private final TicketModelAssembler ticketModelAssembler;
  private final TicketRecordModelAssembler ticketRecordModelAssembler;
  private final UserModelAssembler userModelAssembler;
  private final ModelMapper modelMapper;

  @Autowired
  public TicketController(
      TicketService ticketService,
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

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
  @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<TicketResponseModel> createTicket(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @AuthenticationPrincipal UserPrincipal reporter,
      @Valid TicketRequestModel ticket) {

    TicketDTO requestTicketDTO = modelMapper.map(ticket, TicketDTO.class);
    TicketDTO createdTicket =
        ticketService.createTicket(projectId, requestTicketDTO, reporter.getId());
    TicketResponseModel assembledTicket = ticketModelAssembler.toModel(createdTicket);

    return ResponseEntity.status(HttpStatus.CREATED).body(assembledTicket);
  }

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
  @GetMapping("/{ticketId}")
  @ResponseBody
  public ResponseEntity<TicketResponseModel> getTicket(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId) {

    TicketDTO ticketDto = ticketService.getTicket(ticketId);
    TicketResponseModel assembledTicket = ticketModelAssembler.toModel(ticketDto);

    return ResponseEntity.ok(assembledTicket);
  }

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
  @GetMapping
  @ResponseBody
  public ResponseEntity<DataTablesOutput<TicketResponseModel>> getTickets(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @Valid DataTablesInput input) {

    DataTablesOutput<TicketDTO> pagedTicketsDTOs = ticketService.getTickets(projectId, input);

    DataTablesOutput<TicketResponseModel> pagedTickets =
        ticketModelAssembler.toDataTablesOutputModel(pagedTicketsDTOs);

    return ResponseEntity.ok(pagedTickets);
  }

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "@userServiceImp.isSubscribedToTicket(principal.id, #creatorId)")
  @PatchMapping(value = "/{ticketId}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ResponseBody
  public ResponseEntity<?> updateTicket(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId,
      @AuthenticationPrincipal UserPrincipal reporter,
      @Valid TicketRequestModel ticket) {

    TicketDTO ticketDto = modelMapper.map(ticket, TicketDTO.class);
    ticketService.updateTicket(ticketId, ticketDto, reporter.getId());

    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('SUPER_ADMIN') or" + "#creatorId == principal.id")
  @DeleteMapping("/{ticketId}")
  public ResponseEntity<?> deleteTicket(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId) {

    ticketService.deleteTicket(ticketId);

    return ResponseEntity.noContent().build();
  }

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
  @GetMapping("/{ticketId}/records")
  @ResponseBody
  public ResponseEntity<DataTablesOutput<TicketRecordResponseModel>> getTicketRecords(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId,
      @Valid DataTablesInput input) {
    DataTablesOutput<TicketRecordDTO> ticketRecordsDTOs =
        ticketService.getTicketRecords(ticketId, input);

    DataTablesOutput<TicketRecordResponseModel> ticketRecords =
        ticketRecordModelAssembler.toDataTablesOutputModel(ticketRecordsDTOs);

    return ResponseEntity.ok(ticketRecords);
  }

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
  @GetMapping("/{ticketId}/records/{recordId}")
  public ResponseEntity<TicketRecordResponseModel> getTicketRecord(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId,
      @PathVariable String recordId) {

    TicketRecordDTO ticketRecord = ticketService.getTicketRecord(recordId);

    TicketRecordResponseModel assembledTicketRecord =
        ticketRecordModelAssembler.toModel(ticketRecord);

    return ResponseEntity.ok(assembledTicketRecord);
  }

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "@userServiceImp.isSubscribedToTicket(principal.id, #ticketId)")
  @PostMapping(
      value = "/{ticketId}/comments",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ResponseBody
  public ResponseEntity<?> createComment(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId,
      @AuthenticationPrincipal UserPrincipal creator,
      @Valid CommentRequestModel comment) {

    CommentDTO commentDto = modelMapper.map(comment, CommentDTO.class);
    ticketService.createComment(ticketId, creator.getId(), commentDto);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @PreAuthorize("@ticketAccessEvaluator.areCommentsAllowed(principal.id, #creatorId, #ticketId)")
  @GetMapping("/{ticketId}/comments")
  public ModelAndView getComments(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "limit", defaultValue = "5") int limit,
      @RequestParam(value = "sort", defaultValue = "uploadTime") String sortBy,
      @RequestParam(value = "dir", defaultValue = "DESC") Sort.Direction dir) {

    Page<CommentDTO> pagedCommentsDto =
        ticketService.getComments(ticketId, page, limit, sortBy, dir);

    List<CommentResponseModel> pagedCommentsResponseModel =
        modelMapper.map(
            pagedCommentsDto.getContent(),
            new TypeToken<ArrayList<CommentResponseModel>>() {}.getType());

    ModelAndView model = new ModelAndView("fragments/comments/comments-content");
    model.addObject("limit", limit);
    model.addObject("currentPage", page);
    model.addObject("totalPages", pagedCommentsDto.getTotalPages());
    model.addObject("totalElements", pagedCommentsDto.getTotalElements());
    model.addObject("commentsList", pagedCommentsResponseModel);

    String baseLink =
        linkTo(
                methodOn(TicketController.class)
                    .createComment(creatorId, projectId, ticketId, null, null))
            .toString();

    model.addObject("listRequestLink", baseLink);

    return model;
  }

  @PreAuthorize("hasRole('SUPER_ADMIN') or" + "#creatorId == principal.id")
  @PostMapping(
      value = "/{ticketId}/subscribers",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> addSubscriber(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId,
      @Valid SubscriberRequestModel subscriber) {

    ticketService.addSubscriber(ticketId, subscriber.getPublicId());

    return ResponseEntity.status(HttpStatus.CREATED.value()).build();
  }

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "@userServiceImp.isSubscribedToProject(principal.id, #projectId)")
  @GetMapping("/{ticketId}/subscribers")
  @ResponseBody
  public ResponseEntity<DataTablesOutput<UserResponseModel>> getSubscribers(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId,
      @Valid DataTablesInput input) {

    DataTablesOutput<UserDTO> pagedSubscribersDto = ticketService.getSubscribers(ticketId, input);
    DataTablesOutput<UserResponseModel> assembledSubscribers =
        userModelAssembler.toDataTablesOutputModel(pagedSubscribersDto);

    return ResponseEntity.ok(assembledSubscribers);
  }

  @PreAuthorize(
      "hasRole('SUPER_ADMIN') or"
          + "#creatorId == principal.id or "
          + "#subscriberId == principal.id")
  @DeleteMapping("/{ticketId}/subscribers/{subscriberId}")
  public ResponseEntity<?> removeSubscriber(
      @PathVariable String creatorId,
      @PathVariable String projectId,
      @PathVariable String ticketId,
      @PathVariable String subscriberId) {

    ticketService.removeSubscriber(ticketId, subscriberId);

    return ResponseEntity.noContent().build();
  }
}
