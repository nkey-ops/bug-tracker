package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.ProjectRequestModel;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

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
    public UserResponseModel getCurrentUser(){
        return userController.getCurrentUser();
    }


    /**
     * If the user try to load other page than /home after login or registration
     *  the user model won't be in the session and that page if it requires
     *  user model will throw an Exception like:
     *  ServletRequestBindingException: Missing session attribute user.
     */
    @RequestMapping(value = {"/home", "/"})
    public String home(@ModelAttribute("user") UserResponseModel user,
                       Model model) {
        model.addAttribute("user", user);

        return "index";
    }


    @RequestMapping(value = "/project-form")
    public ModelAndView showProjectForm() {

        ModelAndView mav = new ModelAndView("/forms/project-form",
                "project", new ProjectRequestModel());

        return mav.addObject("user", getCurrentUser());
    }



    @GetMapping(value = "/my-projects")
    public String showProjectList(@ModelAttribute("user") UserResponseModel user,
                                  Model model,
                                  @RequestParam(value = "page", defaultValue = "1") int page,
                                  @RequestParam(value = "limit", defaultValue = "5") int limit) {


        model.addAttribute("user", user);
        projectController.getProjects(model, user.getPublicId(),  page, limit);

        return "/list/my-projects";
    }
}
