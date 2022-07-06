package com.bluesky.bugtraker.view.model.rensponse.assembler;

import com.bluesky.bugtraker.view.controller.BugController;
import com.bluesky.bugtraker.view.controller.ProjectController;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.rensponse.BugResponseModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BugModelAssembler implements RepresentationModelAssembler<BugResponseModel, BugResponseModel> {

    @Override
    public BugResponseModel toModel(BugResponseModel bug) {
        bug.add(
                linkTo(methodOn(UserController.class).
                        getUser(bug.getReportedBy().getPublicId())).withRel("reported by"),

                linkTo(methodOn(ProjectController.class).
                        getProject(bug.getReportedBy().getPublicId(),
                                   bug.getProject().getName()))
                        .withRel("project"),

                linkTo(methodOn(BugController.class).
                        getBug(bug.getReportedBy().getPublicId(),
                                bug.getProject().getName(),
                                bug.getPublicId()))
                        .withSelfRel(),

                linkTo(methodOn(BugController.class)
                        .getBugs(bug.getReportedBy().getPublicId(),
                                bug.getProject().getName(), 1, 15)).
                        withRel("bugs"));
        return bug;
    }

    public CollectionModel<BugResponseModel> toCollectionModelWithSelfRel(
            Set<BugResponseModel> bugResponseModels, String userId, String projectName) {

        CollectionModel<BugResponseModel> result = toCollectionModel(bugResponseModels);
        result.add(
                linkTo(methodOn(BugController.class).getBugs(userId, projectName, 1, 15)).withSelfRel());

        return result;
    }


}
