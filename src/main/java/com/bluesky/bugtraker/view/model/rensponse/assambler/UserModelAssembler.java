package com.bluesky.bugtraker.view.model.rensponse.assambler;

import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.UserDTO;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
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
public  class UserModelAssembler implements RepresentationModelAssembler<UserDTO, UserResponseModel> {
    private final Utils utils;
    private final ModelMapper modelMapper;

    public UserModelAssembler(Utils utils, ModelMapper modelMapper) {
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    @Override
    public UserResponseModel toModel(UserDTO userDTO) {
        UserResponseModel user = modelMapper.map(userDTO, UserResponseModel.class);
        
        return user.add( 
                linkTo(methodOn(UserController.class).getUser(user.getPublicId())).withSelfRel(),
                linkTo(methodOn(UserViewController.class).getUserPage(user.getPublicId())).withRel("page"));
        
    }
   
    public DataTablesOutput<UserResponseModel> toDataTablesOutputModel(DataTablesOutput<UserDTO> input){
        CollectionModel<UserResponseModel> withSelfRel =
                RepresentationModelAssembler.super.toCollectionModel(input.getData());
        DataTablesOutput<UserResponseModel> output = utils.map(input, new TypeToken<>(){});
        output.setData(new ArrayList<>(withSelfRel.getContent()));
        
        
        return output;
    }

}