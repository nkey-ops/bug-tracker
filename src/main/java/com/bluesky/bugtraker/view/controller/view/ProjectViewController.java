package com.bluesky.bugtraker.view.controller.view;

import com.bluesky.bugtraker.view.controller.ProjectController;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.request.CommentRequestModel;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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

    private final UserController userController;

    public ProjectViewController(UserController userController) {
        this.userController = userController;
    }

    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/{projectId}/page")
    public String getProjectPage(@PathVariable String creatorId,
                                 @PathVariable String projectId,
                                 Model model ) {

        WebMvcLinkBuilder baseLink = WebMvcLinkBuilder.linkTo(methodOn(ProjectController.class)
                .getProject(creatorId, projectId));

        model.addAttribute("user", userController.getCurrentUser());    
        model.addAttribute("projectBlockLink", baseLink.slash("/body").toUri().toString());
        
        return "pages/project";
    }

    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/{projectId}/body")
    public String getProjectBody(@PathVariable String creatorId,
                                 @PathVariable String projectId,
                                 Model model ) {

        WebMvcLinkBuilder baseLink = linkTo(methodOn(ProjectController.class)
                .getProject(creatorId, projectId));

        model.addAttribute("selfLink", baseLink.toUri().toString());

        String subscribersLink = baseLink.slash("subscribers").slash("body").toUri().toString();
        model.addAttribute("subscribersLink", subscribersLink);

        String ticketsLink = baseLink.slash("tickets").slash("body").toUri().toString();
        model.addAttribute("ticketsLink", ticketsLink);

        String commentsLink = baseLink.slash("comments").slash("body").toUri().toString();
        model.addAttribute("commentsLink", commentsLink);

        String editFormLink = baseLink.slash("edit").toUri().toString();
        model.addAttribute("projectEditFormLink", editFormLink);
        
        
        return "fragments/list/body/project-body";
    }



    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/body")
    public String getProjectsBody(@PathVariable String creatorId,
                                  Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .toUri().toString();

        model.addAttribute("baseLink", baseLink);

        model.addAttribute("projectContentBlockLink", baseLink + "/content-block");
        model.addAttribute("projectFormBlockLink", baseLink + "/form");

        return "fragments/list/body/projects-body";
    }

    @GetMapping("/content-block")
    public String getProjectsContentBlock(@PathVariable String creatorId,
                                          Model model) {
        String dataSource = linkTo(methodOn(ProjectController.class)
                .getProjects(creatorId, new DataTablesInput()))
                .toUri().toString();

        model.addAttribute("dataSource", dataSource);

        return "fragments/list/content/projects-content";
    }

    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/form")
    public String getProjectForm(@PathVariable String creatorId,
                                 Model model) {
        String baseLink = linkTo(methodOn(ProjectController.class)
                .getProjects(creatorId, new DataTablesInput()))
                .toUri().toString();

        model.addAttribute("projectRequestModel", new ProjectRequestModel());

        model.addAttribute("postRequestLink", baseLink);
        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");
        model.addAttribute("projectContentBlockLink", baseLink + "/content-block");

        return "forms/project-form";
    }

    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/{projectId}/edit")
    public String getProjectEditForm(@PathVariable String creatorId,
                                     @PathVariable String projectId,
                                     Model model) {

        String baseLink = 
                linkTo(methodOn(ProjectController.class)
                .getProject(creatorId, projectId))
                        .toUri().toString();
        
        model.addAttribute("projectRequestModel", new ProjectRequestModel());

        model.addAttribute("postRequestLink", baseLink);

        return "forms/project-edit";
    }


    @PreAuthorize(value = "#creatorId == principal.id")
    @GetMapping("/{projectId}/subscribers/form")
    public String getSubscriberForm(@PathVariable String creatorId,
                                    @PathVariable String projectId,
                                    Model model) {
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
                                             Model model) {

        String dataSource =
                linkTo(methodOn(ProjectController.class)
                        .getSubscribers(creatorId, projectId, new DataTablesInput()))
                        .toUri().toString();

        model.addAttribute("dataSource", dataSource);
        model.addAttribute("subscribersContentBlockLink", dataSource + "/content-block");

        return "fragments/list/content/subscribers-content";
    }

    @PreAuthorize("#creatorId == principal.id")
    @GetMapping("/{projectId}/comments/body")
    public String getCommentsBody(@PathVariable String creatorId,
                                  @PathVariable String projectId,
                                  Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("commentsContentBlockLink", baseLink);
        model.addAttribute("commentFormLink", baseLink + "/form");

        return "fragments/comments/comments-body";
    }

    @PreAuthorize("#creatorId == principal.id")
    @GetMapping("/{projectId}/comments/form")
    public String getCommentForm(@PathVariable String creatorId,
                                 @PathVariable String projectId,
                                 Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("commentsContentBlockLink", baseLink);
        model.addAttribute("postRequestLink", baseLink);
        model.addAttribute("commentRequestModel", new CommentRequestModel());

        return "forms/comment-form";
    }

}
