package com.bluesky.bugtraker.view.controller.view;

import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.view.controller.ProjectController;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.CommentRequestModel;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/users/{creatorId}/projects")
@SessionAttributes("user")
public class ProjectViewController {
    private final UserController userController;

    public ProjectViewController(UserController userController) {
        this.userController = userController;
    }

    @GetMapping("/{projectId}/page")
    public ModelAndView getProjectPage(@PathVariable String creatorId,
                                       @PathVariable String projectId) {

        WebMvcLinkBuilder baseLink = linkTo(methodOn(ProjectController.class).getProject(creatorId, projectId));
        String userPageLink = linkTo(methodOn(UserViewController.class).getUserPage(creatorId)).toString();

        ModelAndView model = new ModelAndView("pages/project");
        model.addObject("user", userController.getCurrentUser());
        model.addObject("userPageLink", userPageLink);
        model.addObject("projectBlockLink", baseLink.slash("/body").toString());
        
        return model;
    }

    @GetMapping("/{projectId}/body")
    public String getProjectBody(@PathVariable String creatorId,
                                 @PathVariable String projectId,
                                 @ModelAttribute("user") UserResponseModel currentUser,
                                 Model model) {

        WebMvcLinkBuilder baseLink = linkTo(methodOn(ProjectController.class)
                .getProject(creatorId, projectId));

        model.addAttribute("selfLink", baseLink.toString());

        String subscribersSourceLink = 
                linkTo(methodOn(ProjectController.class).getSubscribers(creatorId, projectId, null)).toString();
        model.addAttribute("subscriberSourceLink", subscribersSourceLink);
        
        String subscribersLink = baseLink.slash("subscribers").slash("body").toString();
        model.addAttribute("subscribersLink", subscribersLink);

        String ticketsLink = baseLink.slash("tickets").slash("body").toString();
        model.addAttribute("ticketsLink", ticketsLink);

        String commentsLink = baseLink.slash("comments").slash("body").toString();
        model.addAttribute("commentsLink", commentsLink);

        String editFormLink = baseLink.slash("edit").toString();
        model.addAttribute("projectEditFormLink", editFormLink);

        model.addAttribute("user", currentUser);
        model.addAttribute("isCreator", currentUser.getPublicId().equals(creatorId));
        
        return "fragments/list/body/project-body";
    }

    @GetMapping("/body")
    public ModelAndView getProjectsBody(@PathVariable String creatorId) {
        String projectFormLink = linkTo(methodOn(ProjectController.class)
                .getProjects(creatorId, null)).slash("form")
                .toString();
        String projectsContentLink = linkTo(methodOn(ProjectViewController.class)
                .getProjectsContent(creatorId))
                .toString();

        ModelAndView model = new ModelAndView("fragments/list/body/projects-body");
        model.addObject("baseLink", projectFormLink);
        model.addObject("projectContentBlockLink", projectsContentLink);
        model.addObject("projectFormBlockLink", projectFormLink);

        return model;
    }

    @GetMapping("/content-block")
    public ModelAndView getProjectsContent(@PathVariable String creatorId) {
        String dataSource = linkTo(methodOn(ProjectController.class)
                .getProjects(creatorId, new DataTablesInput()))
                .toString();

        ModelAndView model = new ModelAndView("fragments/list/content/projects-content");
        model.addObject("dataSource", dataSource);

        return model;
    }

    @GetMapping("/form")
    public String getProjectForm(@PathVariable String creatorId,
                                 Model model) {
        String baseLink = linkTo(methodOn(ProjectController.class)
                .getProjects(creatorId, new DataTablesInput()))
                .toString();

        model.addAttribute("projectRequestModel", new ProjectRequestModel());

        model.addAttribute("postRequestLink", baseLink);
        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");
        model.addAttribute("projectContentBlockLink", baseLink + "/content-block");

        return "forms/project-form";
    }

    @GetMapping("/{projectId}/edit")
    public String getProjectEditForm(@PathVariable String creatorId,
                                     @PathVariable String projectId,
                                     Model model) {

        String baseLink = linkTo(methodOn(ProjectController.class)
                .getProject(creatorId, projectId))
                .toString();

        model.addAttribute("projectRequestModel", new ProjectRequestModel());
        model.addAttribute("postRequestLink", baseLink);

        return "forms/project-edit";
    }

    @GetMapping("/{projectId}/subscribers/body")
    public String getSubscribersBody(@PathVariable String creatorId,
                                     @PathVariable String projectId,
                                     @AuthenticationPrincipal UserPrincipal principal,
                                     Model model) {

        String baseLink = linkTo(methodOn(ProjectController.class)
                .getSubscribers(creatorId, projectId, null))
                .toString();

        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");
        model.addAttribute("subscriberFormBlockLink", baseLink + "/form");
        model.addAttribute("isCreator", creatorId.equals(principal.getId()));
                
        return "fragments/list/body/subscribers-body";
    }

    @GetMapping("/{projectId}/subscribers/content-block")
    public String getSubscribersContentBlock(@PathVariable String creatorId,
                                             @PathVariable String projectId,
                                             @AuthenticationPrincipal UserPrincipal principal,
                                             Model model) {

        String dataSource =
                linkTo(methodOn(ProjectController.class)
                        .getSubscribers(creatorId, projectId, new DataTablesInput()))
                        .toString();

        model.addAttribute("dataSource", dataSource);
        model.addAttribute("subscribersContentBlockLink", dataSource + "/content-block");
        model.addAttribute("isCreator", creatorId.equals(principal.getId()));
        
        return "fragments/list/content/subscribers-content";
    }

    @GetMapping("/{projectId}/subscribers/form")
    public String getSubscriberForm(@PathVariable String creatorId,
                                    @PathVariable String projectId,
                                    Model model) {
        String baseLink = linkTo(methodOn(ProjectController.class)
                .addSubscriber(creatorId, projectId, new SubscriberRequestModel()))
                .toString();

        model.addAttribute("subscriberRequestModel", new SubscriberRequestModel());
        model.addAttribute("postRequestLink", baseLink);
        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");

        return "forms/subscriber-form";
    }

    @GetMapping("/{projectId}/comments/body")
    public String getCommentsBody(@PathVariable String creatorId,
                                  @PathVariable String projectId,
                                  Model model) {

        String baseLink = linkTo(methodOn(ProjectController.class)
                .createComment(creatorId, projectId, null, null)).toString();

        model.addAttribute("commentsContentBlockLink", baseLink);
        model.addAttribute("commentFormLink", baseLink + "/form");

        return "fragments/comments/comments-body";
    }

    @GetMapping("/{projectId}/comments/form")
    public String getCommentForm(@PathVariable String creatorId,
                                 @PathVariable String projectId,
                                 Model model) {

        String baseLink = linkTo(methodOn(ProjectController.class)
                .createComment(creatorId, projectId, null, null)).toString();

        model.addAttribute("commentsContentBlockLink", baseLink);
        model.addAttribute("postRequestLink", baseLink);
        model.addAttribute("commentRequestModel", new CommentRequestModel());

        String userLink = linkTo(methodOn(UserController.class).getUser(creatorId)).toString();
        model.addAttribute("userLink", userLink);

        return "forms/comment-form";
    }

}
