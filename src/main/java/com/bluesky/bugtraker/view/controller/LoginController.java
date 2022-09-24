package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.view.model.request.UserRegisterModel;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

    @RequestMapping("/signup")
    public ModelAndView signUp() {
        return new ModelAndView("register",
                "user", new UserRegisterModel());
    }

    @GetMapping("/login")
    public String viewLoginPage(
            @RequestParam(value = "error", defaultValue = "false") boolean hasErrors){
        return  "login";
    }

    @GetMapping("/logout")
    public String logout(){
        return "login";
    }

}