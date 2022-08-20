package com.bluesky.bugtraker.view.model.rensponse.assembler;

import com.bluesky.bugtraker.view.controller.TicketController;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BugModelAssembler implements RepresentationModelAssembler<TicketResponseModel, TicketResponseModel> {

    @Override
    public TicketResponseModel toModel(TicketResponseModel bug) {
        bug.add(
                linkTo(methodOn(UserController.class).
                        getUser(bug.getReporter().getPublicId())).withRel("reported by"),

//                linkTo(methodOn(ProjectController.class).
//                        getProject(bug.getProject().getCreator().getPublicId(),
//                                   bug.getProject().getName()))
//                        .withRel("project"),

                linkTo(methodOn(TicketController.class).
                        getBugFixers(bug.getProject().getCreator().getPublicId(),
                                   bug.getProject().getName(),
                                   bug.getPublicId(),
                                   1 , 15))
                        .withRel("bug fixers"));

//                linkTo(methodOn(TicketController.class).
//                        getTicket(bug.getProject().getCreator().getPublicId(),
//                                bug.getProject().getName(),
//                                bug.getPublicId()))
//                        .withSelfRel());

//                linkTo(methodOn(BugController.class)
//                        .getBugs(bug.getProject().getCreator().getPublicId(),
//                                bug.getProject().getName(), 1, 15)).
//                        withRel("bugs"));
        return bug;
    }

    public CollectionModel<TicketResponseModel> toCollectionModelWithSelfRel(
            Set<TicketResponseModel> ticketResponseModels, String userId, String projectName) {

        CollectionModel<TicketResponseModel> result = toCollectionModel(ticketResponseModels);
//        result.add(
//                linkTo(methodOn(BugController.class).getBugs(userId, projectName, 1, 15)).withSelfRel());

        return result;
    }
}
