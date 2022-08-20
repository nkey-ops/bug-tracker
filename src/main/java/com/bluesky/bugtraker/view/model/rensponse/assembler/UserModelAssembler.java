package com.bluesky.bugtraker.view.model.rensponse.assembler;

import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public  class UserModelAssembler implements RepresentationModelAssembler<UserResponseModel, UserResponseModel> {
    @Override
    public UserResponseModel toModel(UserResponseModel user) {
        return user.add(
//                linkTo(methodOn(ProjectController.class).
//                        getProjects(user.getPublicId(), 1, 15)).withRel("user projects"),

                linkTo(methodOn(UserController.class).
                        getSubscribedOnProjects(user.getPublicId(), 1, 15)).withRel("subscribed to projects"),

                linkTo(methodOn(UserController.class).
                        getReportedBugs(user.getPublicId(), 1, 15)).withRel("reported bugs"),

                linkTo(methodOn(UserController.class).
                        getWorkingOnBugs(user.getPublicId(), 1, 15)).withRel("working on bugs"),


                linkTo(methodOn(UserController.class).
                        getUser(user.getPublicId())).withSelfRel());

    }


    @Override
    public CollectionModel<UserResponseModel> toCollectionModel(Iterable<? extends UserResponseModel> entities) {
        return    RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}