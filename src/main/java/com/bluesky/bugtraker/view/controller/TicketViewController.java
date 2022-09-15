package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.shared.ticketstatus.Priority;
import com.bluesky.bugtraker.shared.ticketstatus.Severity;
import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.model.request.TicketRequestModel;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/users/{creatorId}/projects/{projectId}/tickets")
public class TicketViewController {
    
    private  final UserController userController;

    public TicketViewController(UserController userController) {
        this.userController = userController;
    }


    @GetMapping("/body")
    public String getTicketsBody(@PathVariable String creatorId,
                                 @PathVariable String projectId,
                                 Model model) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTickets(creatorId, projectId, new DataTablesInput()))
                        .toUri().toString();

        model.addAttribute("ticketsContentBlockLink", baseLink + "/content-block");
        model.addAttribute("ticketCreationPageLink", baseLink + "/creation");

        return "fragments/list/body/tickets-body";
    }

    @GetMapping("/creation")
    public String getTicketCreationPage(@PathVariable String creatorId,
                                        @PathVariable String projectId, 
                                        Model model) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTickets(creatorId, projectId, new DataTablesInput()))
                        .toUri().toString();
        
        model.addAttribute("user", userController.getCurrentUser());

        model.addAttribute("ticketFormBlockLink", baseLink + "/form");

        return "pages/ticket-creation";
    }


    @GetMapping("/form")
    public String getTicketForm(@PathVariable String creatorId,
                                @PathVariable String projectId,
                                Model model) {

        String baseLink =
                linkTo(methodOn(TicketController.class)
                        .getTickets(creatorId, projectId, new DataTablesInput()))
                        .toUri().toString();

        model.addAttribute("ticketRequestModel", new TicketRequestModel());

        model.addAttribute("statusList", Status.values());
        model.addAttribute("severityList", Severity.values());
        model.addAttribute("priorityList", Priority.values());

        model.addAttribute("postRequestLink", baseLink);

        return "forms/ticket-form";
    }

    @GetMapping("/content-block")
    public String getTicketContentBlock(@PathVariable String creatorId,
                                        @PathVariable String projectId,
                                        Model model) {

        String dataSource =
                linkTo(methodOn(TicketController.class)
                        .getTickets(creatorId, projectId, new DataTablesInput()))
                        .toUri().toString();

        model.addAttribute("dataSource", dataSource);
        model.addAttribute("subscribersContentBlockLink", dataSource + "/content-block");


        return "fragments/list/content/tickets-content";
    }


}
