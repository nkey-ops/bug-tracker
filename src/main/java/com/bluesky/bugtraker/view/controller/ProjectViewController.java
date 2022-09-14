package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/users/{creatorId}/projects")
public class ProjectViewController {


    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/body")
    public String getProjectsBody(@PathVariable String creatorId,
                                  Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .toUri().toString();

        model.addAttribute("baseLink", baseLink);
        model.addAttribute("projectRequestModel", new ProjectRequestModel());
        model.addAttribute("projectContentBlockLink", baseLink + "/content-block");

        return "fragments/list/body/projects-body";
    }

    @GetMapping("/content-block")
    public String getProjectsContentBlock(@PathVariable String creatorId,
                                          Model model) {
//       TODO Throws expection because dataTableInput without values
            String dataSource = linkTo(methodOn(ProjectController.class)
                .getProjects(creatorId, new DataTablesInput()))
                .toUri().toString();

        model.addAttribute("dataSource", dataSource);

        return "fragments/list/content/project-content";
    }
    
    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/{projectId}/subscribers/form")   
    public String getSubscriberForm(@PathVariable String creatorId,
                                    @PathVariable String projectId,
                                    Model model){
        String baseLink = linkTo(methodOn(ProjectController.class)
                .addSubscriber(creatorId, projectId, new SubscriberRequestModel()))
                .toUri().toString();
        
        model.addAttribute("subscriberRequestModel", new SubscriberRequestModel());
        model.addAttribute("postRequestLink", baseLink);
        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");
        
        return "forms/subscriber-form"; 
    }
    
    
    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/{projectId}/subscribers/body")
    public String getSubscribersBody(@PathVariable String creatorId, 
                                     @PathVariable String projectId,
                                     Model model) {
        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("subscribers").toUri().toString();

        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");
        model.addAttribute("subscriberFormBlockLink", baseLink + "/form");

        return "fragments/list/body/subscribers-body";
    }

    @GetMapping("/{projectId}/subscribers/content-block")
    public String getSubscribersContentBlock(@PathVariable String creatorId,
                                             @PathVariable String projectId,
                                             Model model ) {
        
        String dataSource = 
                linkTo(methodOn(ProjectController.class)
                .getSubscribers(creatorId, projectId, new DataTablesInput()))
                .toUri().toString();

        model.addAttribute("dataSource", dataSource);
        model.addAttribute("subscribersContentBlockLink", dataSource + "/content-block");

        return "fragments/list/content/subscribers-content";
    }
}
