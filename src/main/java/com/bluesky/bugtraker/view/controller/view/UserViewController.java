package com.bluesky.bugtraker.view.controller.view;

import com.bluesky.bugtraker.shared.authorizationenum.Role;
import com.bluesky.bugtraker.view.controller.UserController;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping("/users")
public class UserViewController {

    private final UserController userController;

    public UserViewController(UserController userController) {
        this.userController = userController;
    }


    @GetMapping("/content")
    public ModelAndView getUserContent() {
        String dataSource = linkTo(methodOn(UserController.class).getUsers(null)).toString();
        String userContentLink = linkTo(methodOn(UserViewController.class).getUserContent()).toString();
        
        ModelAndView model = new ModelAndView("fragments/list/content/users-content");
        model.addObject("dataSource", dataSource);
        model.addObject("userContentLink", userContentLink);
        model.addObject("roles", Role.values());
        
        return model;
    }
    @GetMapping("/{userId}/profile")
    public ModelAndView getUserPage(@PathVariable String userId) {
        UserResponseModel currentUser = userController.getCurrentUser();
        String userLink = linkTo(methodOn(UserController.class).getUser(userId)).toString();
        String userPageLink = currentUser.getLink("page").orElseThrow().getHref();


        ModelAndView model = new ModelAndView("pages/user");
        model.addObject("userLink", userLink);
        model.addObject("userEditFormLink", userLink + "/edit");
        model.addObject("isCurrentUser", userId.equals(currentUser.getPublicId()));

        model.addObject("user", currentUser);
        model.addObject("userPageLink", userPageLink);
        return model;
    }
    
    @GetMapping("/{userId}/edit")
    public String getUserEditForm(@PathVariable String userId, Model model) {
        String userLink = linkTo(methodOn(UserController.class).getUser(userId)).toString();
        
        model.addAttribute("userLink", userLink);
        model.addAttribute("postRequestLink", userLink);
        model.addAttribute("userModel", new UserResponseModel());
        return "forms/user-edit";
    }
    

    @GetMapping("/{userId}/subscribed-to-projects-body")
    public ModelAndView getSubscribedToProjectsBody(@PathVariable String userId) {
        String subscribedContent = 
                linkTo(methodOn(UserViewController.class).getSubscribedToProjectsContent(userId)).toString();

        ModelAndView model = new ModelAndView("fragments/list/body/user-subscribed-projects-body");
       
        model.addObject("subscribedToContent", subscribedContent);

        return model;
    }

    @GetMapping("/{userId}/subscribed-to-projects-content")
    public ModelAndView getSubscribedToProjectsContent(@PathVariable String userId) {
        String dataSource = 
                linkTo(methodOn(UserController.class).getSubscribedToProjects(userId, null)).toString();

        ModelAndView model = new ModelAndView("fragments/list/content/user-subscribed-to-projects-content");
        model.addObject("dataSource", dataSource);
        
        return model;
    }


    @GetMapping("/{userId}/subscribed-to-tickets-body")
    public ModelAndView getSubscribedToTicketsBody(@PathVariable String userId) {
        String subscribedToTicketsContent = 
                linkTo(methodOn(UserViewController.class).getSubscribedToTicketsContent(userId)).toString();

        ModelAndView model = new ModelAndView("fragments/list/body/user-subscribed-tickets-body");
        model.addObject("subscribedToTicketsContent", subscribedToTicketsContent);

        return model;
    }

    @GetMapping("/{userId}/subscribed-to-tickets-content")
    public ModelAndView getSubscribedToTicketsContent(@PathVariable String userId) {
        String dataSource = 
                linkTo(methodOn(UserController.class).getSubscribedToTickets(userId, null)).toString();

        ModelAndView model = new ModelAndView("fragments/list/content/tickets-content");
        model.addObject("dataSource", dataSource);
        
        return model;
    }

    
    
    
}
