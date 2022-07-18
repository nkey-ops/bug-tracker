package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.security.UserPrincipal;
import com.bluesky.bugtraker.service.UserService;
import com.bluesky.bugtraker.shared.dto.UserDto;
import com.bluesky.bugtraker.view.model.rensponse.UserResponseModel;
import com.bluesky.bugtraker.view.model.request.UserRequestModel;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    private final UserService userService;
    private final ModelMapper modelMapper = new ModelMapper();

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String viewLoginPage(
            @RequestParam(value = "error", defaultValue = "false")
            boolean isError) {
        return isError ? "error-404" : "login";
    }

    @RequestMapping("/home")
    public ModelAndView home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = ((UserPrincipal) auth.getPrincipal()).getId();

        UserResponseModel user = modelMapper.map(
                userService.getUserById(userId),
                UserResponseModel.class);


        return new ModelAndView("index",
                "user", user);
    }


    @RequestMapping("/signup")
    public ModelAndView signUp() {
        return new ModelAndView("register",
                "user", new UserRequestModel());
    }
}
