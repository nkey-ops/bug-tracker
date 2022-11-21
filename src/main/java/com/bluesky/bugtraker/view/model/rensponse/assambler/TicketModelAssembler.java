package com.bluesky.bugtraker.view.model.rensponse.assambler;

import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.TicketDTO;
import com.bluesky.bugtraker.view.controller.TicketController;
import com.bluesky.bugtraker.view.controller.view.TicketViewController;
import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import org.modelmapper.TypeToken;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TicketModelAssembler implements RepresentationModelAssembler<TicketResponseModel, TicketResponseModel> {

    private final Utils utils;

    public TicketModelAssembler(Utils utils) {
        this.utils = utils;
    }
    
    @Override
    public TicketResponseModel toModel(TicketResponseModel ticket) {
        ticket.add(
                linkTo(methodOn(UserViewController.class).getUserPage(ticket.getReporter().getPublicId()))
                        .withRel("creator page"),
                linkTo(methodOn(TicketViewController.class).
                        getTicketPage(ticket.getProject().getCreator().getPublicId(),
                                ticket.getProject().getPublicId(),
                                ticket.getPublicId()))
                        .withRel("page"),
                linkTo(methodOn(TicketController.class)
                        .getTickets(ticket.getProject().getCreator().getPublicId(), 
                                    ticket.getProject().getPublicId(), null))
                        .withRel("tickets"));
        return ticket;
    }

    public DataTablesOutput<TicketResponseModel> toDataTablesOutputModel(DataTablesOutput<TicketDTO> input){
        DataTablesOutput<TicketResponseModel> output =
                utils.map(input, new TypeToken<>() {});

        CollectionModel<TicketResponseModel> withSelfRel =
                RepresentationModelAssembler.super.toCollectionModel(output.getData());

        output.setData(new ArrayList<>(withSelfRel.getContent()));

        return output;
    }
    
}
