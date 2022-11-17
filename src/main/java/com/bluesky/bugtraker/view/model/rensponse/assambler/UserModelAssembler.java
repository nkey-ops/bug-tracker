package com.bluesky.bugtraker.view.model.rensponse.assambler;

import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
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
public  class UserModelAssembler implements RepresentationModelAssembler<UserResponseModel, UserResponseModel> {
    private final Utils utils;

    public UserModelAssembler(Utils utils) {
        this.utils = utils;
    }

    @Override
    public UserResponseModel toModel(UserResponseModel user) {
        return user.add( 
                linkTo(methodOn(UserController.class).getUser(user.getPublicId())).withSelfRel(),
                linkTo(methodOn(UserViewController.class).getUserPage(user.getPublicId())).withRel("page"));
        
    }

    @Override
    public CollectionModel<UserResponseModel> toCollectionModel(Iterable<? extends UserResponseModel> entities) {
        return    RepresentationModelAssembler.super.toCollectionModel(entities);
    }

    public DataTablesOutput<UserResponseModel> toDataTablesOutputModel(DataTablesOutput<UserDTO> input){
        DataTablesOutput<UserResponseModel> output =
                utils.map(input, new TypeToken<List<UserResponseModel>>() {});

        CollectionModel<UserResponseModel> withSelfRel =
                RepresentationModelAssembler.super.toCollectionModel(output.getData());

        output.setData(new ArrayList<>(withSelfRel.getContent()));

        return output;
    }

}