package com.bluesky.bugtraker.view.model.rensponse.assambler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.TicketDTO;
import com.bluesky.bugtraker.view.controller.TicketController;
import com.bluesky.bugtraker.view.controller.view.TicketViewController;
import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import java.util.ArrayList;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TicketModelAssembler
    implements RepresentationModelAssembler<TicketDTO, TicketResponseModel> {

  private final Utils utils;
  private final ModelMapper modelMapper;

  public TicketModelAssembler(Utils utils, ModelMapper modelMapper) {
    this.utils = utils;
    this.modelMapper = modelMapper;
  }

  @Override
  public TicketResponseModel toModel(TicketDTO ticketDTO) {
    TicketResponseModel ticket = modelMapper.map(ticketDTO, TicketResponseModel.class);

    ticket.add(
        linkTo(methodOn(UserViewController.class).getUserPage(ticket.getReporter().getPublicId()))
            .withRel("creator page"),
        linkTo(
                methodOn(TicketViewController.class)
                    .getTicketPage(
                        ticket.getProject().getCreator().getPublicId(),
                        ticket.getProject().getPublicId(),
                        ticket.getPublicId()))
            .withRel("page"),
        linkTo(
                methodOn(TicketController.class)
                    .getTickets(
                        ticket.getProject().getCreator().getPublicId(),
                        ticket.getProject().getPublicId(),
                        null))
            .withRel("tickets"));
    return ticket;
  }

  public DataTablesOutput<TicketResponseModel> toDataTablesOutputModel(
      DataTablesOutput<TicketDTO> input) {
    CollectionModel<TicketResponseModel> withSelfRel =
        RepresentationModelAssembler.super.toCollectionModel(input.getData());
    DataTablesOutput<TicketResponseModel> output = utils.map(input, new TypeToken<>() {});
    output.setData(new ArrayList<>(withSelfRel.getContent()));

    return output;
  }
}
