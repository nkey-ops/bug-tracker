package com.bluesky.bugtraker.view.model.rensponse.assembler;

import com.bluesky.bugtraker.view.controller.ProjectController;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
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
                linkTo(methodOn(ProjectController.class).
                        getProjects(user.getPublicId(), 1, 15) ).withRel("projects"),
                linkTo(methodOn(UserController.class).
                        getUser(user.getPublicId())).withSelfRel());

    }
}