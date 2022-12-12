package com.bluesky.bugtraker.view.model.rensponse.assambler;

import com.bluesky.bugtraker.service.utils.Utils;
import com.bluesky.bugtraker.shared.dto.ProjectDTO;
import com.bluesky.bugtraker.view.controller.ProjectController;
import com.bluesky.bugtraker.view.controller.view.ProjectViewController;
import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.ProjectResponseModel;
import com.bluesky.bugtraker.view.model.rensponse.TicketResponseModel;
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
public class ProjectModelAssembler implements
        RepresentationModelAssembler<ProjectDTO, ProjectResponseModel> {
    private final Utils utils;
    private  final ModelMapper modelMapper;

    public ProjectModelAssembler(Utils utils, 
                                 ModelMapper modelMapper) {
        this.utils = utils;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProjectResponseModel toModel(ProjectDTO projectDTO) {
        ProjectResponseModel project = 
                modelMapper.map(projectDTO, ProjectResponseModel.class);
        
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

        CollectionModel<ProjectResponseModel> withSelfRel =
                RepresentationModelAssembler.super.toCollectionModel(input.getData());
        DataTablesOutput<ProjectResponseModel> output = utils.map(input, new TypeToken<>(){});
        output.setData(new ArrayList<>(withSelfRel.getContent()));
        
        return output;
    }

}