package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.view.model.request.UserRegisterModel;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    @GetMapping("/signup")
    public ModelAndView signUp() {
        return new ModelAndView("register",
                "userRegisterModel", new UserRegisterModel());
    }

    @GetMapping("/login")
    public String viewLoginPage(){
        return  "login";
    }

    @GetMapping("/logout")
    public String logout(){
        return "login";
    }

}