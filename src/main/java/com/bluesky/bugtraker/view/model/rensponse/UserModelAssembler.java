package com.bluesky.bugtraker.view.model.rensponse;

import com.bluesky.bugtraker.view.controller.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public  class UserModelAssembler implements RepresentationModelAssembler<UserResponseModel, EntityModel<UserResponseModel>> {
    @Override
    public EntityModel<UserResponseModel> toModel(UserResponseModel user) {

        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).getUser(user.getPublicId())).withSelfRel());
    }
}