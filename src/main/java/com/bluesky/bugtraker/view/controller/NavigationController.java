package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.shared.ticketstatus.Status;
import com.bluesky.bugtraker.view.controller.view.ProjectViewController;
import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
public class NavigationController {
    private final UserController userController;


    public NavigationController(UserController userController) {
        this.userController = userController;
    }

    @ModelAttribute("user")
    public UserResponseModel getCurrentUser() {
        return userController.getCurrentUser();
    }

    @RequestMapping(value = {"/home", "/"})
    public String home(@ModelAttribute("user") UserResponseModel user,
                       Model model) {
        
        String userId = user.getPublicId();

        String ticketsInfoLink = linkTo(methodOn(UserController.class).getTicketsInfo(userId)).toString();
        String userInfo = linkTo(methodOn(UserController.class).getUserInfo(userId)).toString();
        String userPageLink = linkTo(methodOn(UserViewController.class).getUserPage(userId)).toString();

        model.addAttribute("user", user);
        model.addAttribute("ticketsInfoLink", ticketsInfoLink);
        model.addAttribute("userInfoLink", userInfo);
        model.addAttribute("userPageLink", userPageLink);
        model.addAttribute("ticketStatus", Status.values());

        
        return "index";
    }

    @GetMapping("/lists")
    public  String getListsPage(Model model){
        String userId = userController.getCurrentUser().getPublicId();

        String userPageLink = 
                linkTo(methodOn(UserViewController.class).getUserPage(userId)).toString();
        String myProjectsBodyLink = 
                linkTo(methodOn(ProjectViewController.class).getProjectsBody(userId)).toString();
        String subscribedToProjectsLink = 
                linkTo(methodOn(UserViewController.class).getSubscribedToProjectsBody(userId)).toString();
        String subscribedToTicketsLink =         
                linkTo(methodOn(UserViewController.class).getSubscribedToTicketsBody(userId)).toString();

        model.addAttribute("userPageLink", userPageLink);
        model.addAttribute("myProjectsBodyLink", myProjectsBodyLink);
        model.addAttribute("subscribedToProjectsLink", subscribedToProjectsLink);
        model.addAttribute("subscribedToTicketsLink", subscribedToTicketsLink);

        return "pages/lists";
    }

    @GetMapping("/users-management")
    public String getUserManagementPage(Model model) {
        String userId = userController.getCurrentUser().getPublicId();
        
        String userContent = linkTo(methodOn(UserViewController.class).getUserContent()).toString();
        String userPageLink = linkTo(methodOn(UserViewController.class).getUserPage(userId)).toString();
        
        model.addAttribute("userContentLink", userContent);
        model.addAttribute("user", userController.getCurrentUser());
        model.addAttribute("userPageLink", userPageLink);
        
        return "pages/users-management";
    }

    @GetMapping("/my-projects")
    public String getMyProjectsPage(Model model) {
        String userId = userController.getCurrentUser().getPublicId();

        String myProjectsContentLink = linkTo(methodOn(ProjectViewController.class).getProjectsBody(userId)).toString();
        String userPageLink = linkTo(methodOn(UserViewController.class).getUserPage(userId)).toString();
        
        model.addAttribute("pageTitle", "My Projects");
        model.addAttribute("contentLink", myProjectsContentLink);
        
        model.addAttribute("user", userController.getCurrentUser());
        model.addAttribute("userPageLink", userPageLink);

        return "pages/blank-page";
    }

    @GetMapping("/subscribed-to-projects")
    public String getSubscribedToProjectsPage(Model model) {
        String userId = userController.getCurrentUser().getPublicId();
        
        String subscribedToProjectsContent =
                linkTo(methodOn(UserViewController.class).getSubscribedToProjectsBody(userId)).toString();
        String userPageLink = linkTo(methodOn(UserViewController.class).getUserPage(userId)).toString();
        
        model.addAttribute("pageTitle", "Project Subscriptions");
        model.addAttribute("contentLink", subscribedToProjectsContent);
        
        model.addAttribute("user", userController.getCurrentUser());
        model.addAttribute("userPageLink", userPageLink);

        return "pages/blank-page";
    }

    @GetMapping("/subscribed-to-tickets")
    public String getSubscribedToTicketsPage(Model model) {
        String userId = userController.getCurrentUser().getPublicId();
        
        String subscribedToTicketsContent =
                linkTo(methodOn(UserViewController.class).getSubscribedToTicketsBody(userId)).toString();
        String userPageLink = linkTo(methodOn(UserViewController.class).getUserPage(userId)).toString();
        
        model.addAttribute("pageTitle", "Ticket Subscriptions");
        model.addAttribute("contentLink", subscribedToTicketsContent);
        
        model.addAttribute("user", userController.getCurrentUser());
        model.addAttribute("userPageLink", userPageLink);
        
        return "pages/blank-page";
    }
}
