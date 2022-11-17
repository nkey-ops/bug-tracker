package com.bluesky.bugtraker.view.model.rensponse.assambler;

import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.view.controller.ProjectController;
import com.bluesky.bugtraker.view.controller.view.ProjectViewController;
import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
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
public class ProjectModelAssembler implements
        RepresentationModelAssembler<ProjectResponseModel, ProjectResponseModel> {
    private final Utils utils;

    public ProjectModelAssembler(Utils utils) {
        this.utils = utils;
    }

    @Override
    public ProjectResponseModel toModel(ProjectResponseModel project) {
        return project.add(
                linkTo(methodOn(ProjectController.class)
                                .getProject(project.getCreator().getPublicId(), project.getPublicId())).withSelfRel(),
                linkTo(methodOn(ProjectViewController.class)
                        .getProjectPage(project.getCreator().getPublicId(), project.getPublicId())).withRel("page"),
                linkTo(methodOn(ProjectController.class)
                        .getProjects(project.getCreator().getPublicId(), null)).withRel("projects"),
                linkTo(methodOn(UserViewController.class)
                        .getUserPage(project.getCreator().getPublicId())).withRel("creatorPage"));
    }
    
    
    public DataTablesOutput<ProjectResponseModel> toDataTablesOutputModel(DataTablesOutput<ProjectDTO> input){
        DataTablesOutput<ProjectResponseModel> output =
                utils.map(input, new TypeToken<List<ProjectResponseModel>>() {});

        CollectionModel<ProjectResponseModel> withSelfRel =
                RepresentationModelAssembler.super.toCollectionModel(output.getData());

        output.setData(new ArrayList<>(withSelfRel.getContent()));
        
        return output;
    }

}