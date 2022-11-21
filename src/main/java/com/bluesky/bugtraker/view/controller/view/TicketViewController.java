package com.bluesky.bugtraker.view.controller.view;

import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.security.accessevaluator.TicketAccessEvaluator;
import com.bluesky.bugtraker.service.TicketService;
import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.controller.TicketController;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.CommentRequestModel;
import com.bluesky.bugtraker.view.model.request.SubscriberRequestModel;
import com.bluesky.bugtraker.view.model.request.TicketRequestModel;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/users/{creatorId}/projects/{projectId}/tickets")
public class TicketViewController {

    private final UserController userController;
    private final TicketAccessEvaluator ticketAccessEvaluator;

    public TicketViewController(UserController userController, 
                                TicketAccessEvaluator ticketAccessEvaluator) {
        this.userController = userController;
        this.ticketAccessEvaluator = ticketAccessEvaluator;
    }

    @PreAuthorize("#creatorId == principal.id or @userServiceImp.isSubscribedToProject(principal.id, #projectId)")
    @GetMapping("/{ticketId}/page")
    public ModelAndView getTicketPage(@PathVariable String creatorId,
                                      @PathVariable String projectId,
                                      @PathVariable String ticketId) {

        ModelAndView modelAndView = new ModelAndView("pages/tickets/ticket");
        WebMvcLinkBuilder ticketLink = linkTo(methodOn(TicketController.class).getTicket(creatorId, projectId, ticketId));

        String projectPageLink = linkTo(methodOn(ProjectViewController.class).getProjectPage(creatorId, projectId)).toString();
        String subscribersSourceLink = linkTo(methodOn(TicketController.class).getSubscribers(creatorId, projectId, ticketId, null)).toString();
        String ticketDetailsLink = ticketLink.slash("details").toString();
        String ticketCommentsLink = ticketLink.slash("comments").slash("body").toString();
        String ticketRecordsLink = ticketLink.slash("records").slash("body").toString();
        String ticketSubscribersLink = linkTo(methodOn(TicketViewController.class).getSubscribersBody(creatorId, projectId, ticketId)).toString();
        String userPageLink = linkTo(methodOn(UserViewController.class).getUserPage(creatorId)).toString();

        UserResponseModel currentUser = userController.getCurrentUser();
        boolean areCommentsAllowed = ticketAccessEvaluator.areCommentsAllowed(currentUser.getPublicId(), creatorId, ticketId);
        boolean isUserSubscribed = ticketAccessEvaluator.isUserSubscribed(currentUser.getPublicId(), ticketId);

        modelAndView.addObject("ticketLink", ticketLink.toString());
        modelAndView.addObject("projectLink", projectPageLink);
        modelAndView.addObject("subscribersSourceLink", subscribersSourceLink);
        modelAndView.addObject("selfLink", ticketLink.toString());
        modelAndView.addObject("ticketDetailsLink", ticketDetailsLink);
        modelAndView.addObject("ticketCommentsLink", ticketCommentsLink);
        modelAndView.addObject("ticketRecordsLink", ticketRecordsLink);
        modelAndView.addObject("ticketSubscribersLink", ticketSubscribersLink);

        modelAndView.addObject("userPageLink", userPageLink);
        modelAndView.addObject("user", currentUser);
        modelAndView.addObject("areCommentsAllowed", areCommentsAllowed);
        modelAndView.addObject("isUserSubscribed", isUserSubscribed);
        modelAndView.addObject("isCreator", creatorId.equals(currentUser.getPublicId()));

        return modelAndView;
    }


    @GetMapping("/creation")
    public String getTicketCreationPage(@PathVariable String creatorId,
                                        @PathVariable String projectId,
                                        Model model) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTickets(creatorId, projectId, new DataTablesInput())).toString();

        UserResponseModel currentUser = userController.getCurrentUser();
        String userPageLink = linkTo(methodOn(UserViewController.class).getUserPage(currentUser.getPublicId())).toString();

        model.addAttribute("userPageLink", userPageLink);
        model.addAttribute("user", currentUser);
        model.addAttribute("ticketFormBlockLink", baseLink + "/form");

        return "pages/tickets/ticket-creation";
    }

    @GetMapping("/{ticketId}/edit")
    public String getTicketEditionPage(@PathVariable String creatorId,
                                       @PathVariable String projectId,
                                       @PathVariable String ticketId,
                                       Model model) {
        UserResponseModel currentUser = userController.getCurrentUser();

        String baseLink =
                linkTo(methodOn(TicketController.class).getTicket(creatorId, projectId, ticketId)).toString();
        String ticketPageLink =
                linkTo(methodOn(TicketViewController.class).getTicketPage(creatorId, projectId, ticketId)).toString();
        String userPageLink =
                linkTo(methodOn(UserViewController.class).getUserPage(currentUser.getPublicId())).toString();

        model.addAttribute("ticketFormBlockLink", baseLink + "/edit-form");
        model.addAttribute("ticketPageLink", ticketPageLink);
        model.addAttribute("userPageLink", userPageLink);
        model.addAttribute("user", currentUser);

        return "pages/tickets/ticket-edition";
    }

    @GetMapping("/{ticketId}/details")
    public ModelAndView getTicketDetails(@PathVariable String creatorId,
                                   @PathVariable String projectId,
                                   @PathVariable String ticketId) {

        String ticketLink = linkTo(methodOn(TicketController.class)
                .getTicket(creatorId, projectId, ticketId)).toString();
        String ticketEditFormLink = linkTo(methodOn(TicketController.class)
                .getTicket(creatorId, projectId, ticketId)).slash("/edit").toString();

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean isEditionAllowed = ticketAccessEvaluator.isTicketEditionAllowed(principal.getId(), creatorId, ticketId);

        ModelAndView modelAndView = new ModelAndView("details/ticket-details");
        modelAndView.addObject("ticketEditFormLink", ticketEditFormLink);
        modelAndView.addObject("ticketLink", ticketLink);
        modelAndView.addObject("isMainTicket", true);
        modelAndView.addObject("isEditionAllowed", isEditionAllowed);

        return modelAndView;
    }

    @GetMapping("/body")
    public ModelAndView getTicketsBody(@PathVariable String creatorId,
                                       @PathVariable String projectId) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTickets(creatorId, projectId, new DataTablesInput()))
                        .toString();

        ModelAndView model = new ModelAndView("fragments/list/body/tickets-body");
        model.addObject("ticketsContentBlockLink", baseLink + "/content-block");
        model.addObject("ticketCreationPageLink", baseLink + "/creation");

        return model;
    }

    @GetMapping("/content-block")
    public String getTicketContentBlock(@PathVariable String creatorId,
                                        @PathVariable String projectId,
                                        Model model) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTickets(creatorId, projectId, new DataTablesInput()))
                        .toString();

        model.addAttribute("dataSource", baseLink);
        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");


        return "fragments/list/content/tickets-content";
    }

    @GetMapping("/form")
    public String getTicketForm(@PathVariable String creatorId,
                                @PathVariable String projectId,
                                Model model) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTickets(creatorId, projectId, new DataTablesInput()))
                        .toString();

        model.addAttribute("ticketRequestModel", new TicketRequestModel());

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());

        model.addAttribute("postRequestLink", baseLink);

        return "forms/ticket-form";
    }

    @GetMapping("/{ticketId}/edit-form")
    public String getTicketEditForm(@PathVariable String creatorId,
                                    @PathVariable String projectId,
                                    @PathVariable String ticketId,
                                    Model model) {
        String ticketLink = linkTo(methodOn(TicketController.class)
                .getTicket(creatorId, projectId, ticketId)).toString();

        model.addAttribute("ticketLink", ticketLink);
        model.addAttribute("postRequestLink", ticketLink);
        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());

        model.addAttribute("ticketRequestModel", new TicketRequestModel());

        return "forms/ticket-edit";
    }

    @GetMapping("/{ticketId}/records/{recordId}/details")
    public ModelAndView getTicketRecordDetails(@PathVariable String creatorId,
                                         @PathVariable String projectId,
                                         @PathVariable String ticketId,
                                         @PathVariable String recordId) {
        String ticketRecordLink = 
                linkTo(methodOn(TicketController.class)
                        .getTicketRecord(creatorId, projectId, ticketId, recordId)).toString();

        ModelAndView modelAndView = new ModelAndView("details/ticket-details");
        modelAndView.addObject("ticketLink", ticketRecordLink);
        modelAndView.addObject("isMainTicket", false);

        return modelAndView;
    }


    @GetMapping("/{ticketId}/records/body")
    public String getTicketRecordsBody(@PathVariable String creatorId,
                                       @PathVariable String projectId,
                                       @PathVariable String ticketId,
                                       Model model) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTicketRecords(creatorId, projectId, ticketId, new DataTablesInput()))
                        .toString();

        model.addAttribute("ticketRecordsContentBlockLink", baseLink + "/content-block");

        return "fragments/list/body/ticket-records-body";
    }

    @GetMapping("/{ticketId}/records/content-block")
    public String getTicketRecordsContentBlock(@PathVariable String creatorId,
                                               @PathVariable String projectId,
                                               @PathVariable String ticketId,
                                               Model model) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTicketRecords(creatorId, projectId, ticketId, new DataTablesInput()))
                        .toString();

        model.addAttribute("dataSource", baseLink);

        return "fragments/list/content/ticket-records-content";
    }

    @GetMapping("/{ticketId}/subscribers/body")
    public ModelAndView getSubscribersBody(@PathVariable String creatorId,
                                           @PathVariable String projectId,
                                           @PathVariable String ticketId) {

        String baseLink = linkTo(methodOn(TicketController.class)
                .getSubscribers(creatorId, projectId, ticketId, new DataTablesInput())).toString();
        UserPrincipal principal =
                (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ModelAndView modelAndView = new ModelAndView("fragments/list/body/subscribers-body");

        modelAndView.addObject("subscribersContentBlockLink", baseLink + "/content-block");
        modelAndView.addObject("subscriberFormBlockLink", baseLink + "/form");
        modelAndView.addObject("isCreator", creatorId.equals(principal.getId()));
        return modelAndView;
    }

    @GetMapping("/{ticketId}/subscribers/form")
    public String getSubscriberForm(@PathVariable String creatorId,
                                    @PathVariable String projectId,
                                    @PathVariable String ticketId,
                                    Model model) {
        String baseLink = linkTo(methodOn(TicketController.class)
                .getSubscribers(creatorId, projectId, ticketId, new DataTablesInput()))
                .toString();

        model.addAttribute("subscriberRequestModel", new SubscriberRequestModel());
        model.addAttribute("postRequestLink", baseLink);
        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");

        return "forms/subscriber-form";
    }

    @GetMapping("/{ticketId}/subscribers/content-block")
    public String getSubscribersContentBlock(@PathVariable String creatorId,
                                             @PathVariable String projectId,
                                             @PathVariable String ticketId,
                                             @AuthenticationPrincipal UserPrincipal principal,
                                             Model model) {

        String baseLink = linkTo(methodOn(TicketController.class)
                .getSubscribers(creatorId, projectId, ticketId, new DataTablesInput()))
                .toString();

        model.addAttribute("dataSource", baseLink);
        model.addAttribute("subscribersContentBlockLink", baseLink + "/content-block");
        model.addAttribute("isCreator", creatorId.equals(principal.getId()));

        return "fragments/list/content/subscribers-content";
    }

    @GetMapping("/{ticketId}/comments/form")
    public String getCommentForm(@PathVariable String creatorId,
                                 @PathVariable String projectId,
                                 @PathVariable String ticketId,
                                 Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("tickets")
                .slash(ticketId)
                .slash("comments")
                .toString();

        model.addAttribute("commentsContentBlockLink", baseLink);
        model.addAttribute("postRequestLink", baseLink);
        model.addAttribute("commentRequestModel", new CommentRequestModel());

        String userLink = linkTo(methodOn(UserController.class).getUser(creatorId)).toString();
        model.addAttribute("userLink", userLink);

        return "forms/comment-form";
    }

    @GetMapping("/{ticketId}/comments/body")
    public String getCommentsBody(@PathVariable String creatorId,
                                  @PathVariable String projectId,
                                  @PathVariable String ticketId,
                                  Model model) {

        String baseLink = linkTo(UserController.class)
                .slash(creatorId)
                .slash("projects")
                .slash(projectId)
                .slash("tickets")
                .slash(ticketId)
                .slash("comments")
                .toUri().toString();

        model.addAttribute("commentsContentBlockLink", baseLink);
        model.addAttribute("commentFormLink", baseLink + "/form");

        return "fragments/comments/comments-body";
    }

}
