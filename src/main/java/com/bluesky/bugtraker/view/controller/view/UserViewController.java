package com.bluesky.bugtraker.view.controller.view;

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


    @GetMapping("/{userId}/profile")
    public ModelAndView getUserPage(@PathVariable String userId) {
        String userLink = linkTo(methodOn(UserController.class).getUser(userId)).toUri().toString();

        ModelAndView model = new ModelAndView("pages/user");
        model.addObject("userLink", userLink);
        model.addObject("user", userController.getCurrentUser());
        model.addObject("userEditFormLink", userLink + "/edit");
        
        return model;
    }
    @GetMapping("/{userId}/edit")
    public String getUserEditForm(@PathVariable String userId, Model model) {
        String userLink = linkTo(methodOn(UserController.class).getUser(userId)).toUri().toString();
        
        model.addAttribute("userLink", userLink);
        model.addAttribute("postRequestLink", userLink);
        model.addAttribute("userModel", new UserResponseModel());
        return "forms/user-edit";
    }
}
