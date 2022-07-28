package com.bluesky.bugtraker.view.model.rensponse.assembler;

import com.bluesky.bugtraker.view.controller.BugController;
import com.bluesky.bugtraker.view.controller.ProjectController;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProjectModelAssembler implements
                        RepresentationModelAssembler<ProjectResponseModel, ProjectResponseModel> {
    @Override
    public ProjectResponseModel toModel(ProjectResponseModel project) {
        project.add(
                linkTo(methodOn(UserController.class).
                        getUser(project.getCreator().getPublicId())).withRel("creator"),

                linkTo(methodOn(BugController.class).
                        getBugs(project.getCreator().getPublicId(), project.getName(), 1, 15)).withRel("list of bugs"),

                linkTo(methodOn(ProjectController.class).
                        getSubscribers(project.getCreator().getPublicId(), project.getName(), 1, 15)).withRel("list of subscribers"));

//                linkTo(methodOn(ProjectController.class).
//                        getProject(project.getCreator().getPublicId(), project.getName())).withSelfRel());

//                linkTo(methodOn(ProjectController.class)
//                        .getProjects(project.getCreator().getPublicId(), 1, 15)).withRel("list of projects"));


        return project;
    }

    public CollectionModel<ProjectResponseModel> toCollectionModelWithSelfRel(
            Set<ProjectResponseModel> projectResponseModels, String userId) {

        CollectionModel<ProjectResponseModel> result = toCollectionModel(projectResponseModels);
//        result.add(
//                linkTo(methodOn(ProjectController.class).getProjects(userId, 1, 15)).withSelfRel());

        return result;
    }

}