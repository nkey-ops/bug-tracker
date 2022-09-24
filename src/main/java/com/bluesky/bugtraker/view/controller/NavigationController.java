package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.view.controller.view.UserViewController;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
public class NavigationController {
    private final UserController userController;
    private final ProjectController projectController;

    private final ModelMapper modelMapper = new ModelMapper();


    public NavigationController(UserController userController, ProjectController projectController) {
        this.userController = userController;
        this.projectController = projectController;
    }

    @ModelAttribute("user")
    public UserResponseModel getCurrentUser() {
        return userController.getCurrentUser();
    }


    /**
     * If the user try to load other page than /home after login or registration
     * the user model won't be in the session and that page if it requires
     * user model will throw an Exception like:
     * ServletRequestBindingException: Missing session attribute user.
     */
    @RequestMapping(value = {"/home", "/"})
    public String home(@ModelAttribute("user") UserResponseModel user,
                       Model model) {

        String myProjectsLink = linkTo(UserController.class)
                .slash(user.getPublicId())
                .slash("projects")
                .slash("body")
                .toUri().toString();

        model.addAttribute("user", user);
        model.addAttribute("projectsLink", myProjectsLink);

        String ticketsInfoLink =
                linkTo(methodOn(UserController.class)
                        .getTicketsInfo(user.getPublicId()))
                        .toUri().toString();
        model.addAttribute("ticketsInfoLink", ticketsInfoLink);

        String userInfo =
                linkTo(methodOn(UserController.class)
                        .getUserInfo(user.getPublicId()))
                        .toUri().toString();
        model.addAttribute("userInfoLink", userInfo);


        String userPageLink =
                linkTo(methodOn(UserViewController.class)
                        .getUserPage(user.getPublicId()))
                        .toUri().toString();

        model.addAttribute("userPageLink", userPageLink);

        return "index";
    }


}
