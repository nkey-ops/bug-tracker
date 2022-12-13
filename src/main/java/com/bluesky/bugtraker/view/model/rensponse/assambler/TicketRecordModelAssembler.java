package com.bluesky.bugtraker.view.model.rensponse.assambler;

import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.TicketRecordDTO;
import com.bluesky.bugtraker.view.controller.view.TicketViewController;
import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketRecordResponseModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TicketRecordModelAssembler implements RepresentationModelAssembler<TicketRecordDTO, TicketRecordResponseModel> {

    private final Utils utils;
    private final ModelMapper modelMapper;
    public TicketRecordModelAssembler(Utils utils, ModelMapper modelMapper) {
        this.utils = utils;
        this.modelMapper = modelMapper;
    }
    
    
    @Override
    public TicketRecordResponseModel toModel(TicketRecordDTO ticketRecordDTO) {
        TicketRecordResponseModel ticketRecord =
                modelMapper.map(ticketRecordDTO, TicketRecordResponseModel.class);

        ticketRecord.add(
                linkTo(methodOn(UserViewController.class).getUserPage(ticketRecord.getCreator().getPublicId()))
                        .withRel("creatorPage"),
                linkTo(methodOn(TicketViewController.class)
                        .getTicketRecordDetails(
                                ticketRecord.getMainTicket().getProject().getCreator().getPublicId(),
                                ticketRecord.getMainTicket().getProject().getPublicId(),
                                ticketRecord.getMainTicket().getPublicId(),
                                ticketRecord.getPublicId()))
                        .withRel("details"),
                linkTo(methodOn(TicketViewController.class).getTicketDetails(
                        ticketRecord.getMainTicket().getProject().getCreator().getPublicId(),
                        ticketRecord.getMainTicket().getProject().getPublicId(),
                        ticketRecord.getMainTicket().getPublicId()))
                        .withRel("mainTicketDetails"));
                
        return ticketRecord;
    }

    public DataTablesOutput<TicketRecordResponseModel> toDataTablesOutputModel(DataTablesOutput<TicketRecordDTO> input){
        CollectionModel<TicketRecordResponseModel> withSelfRel =
                RepresentationModelAssembler.super.toCollectionModel(input.getData());
        DataTablesOutput<TicketRecordResponseModel> output = utils.map(input, new TypeToken<>(){});
        output.setData(new ArrayList<>(withSelfRel.getContent()));

        return output;
    }
    
}
