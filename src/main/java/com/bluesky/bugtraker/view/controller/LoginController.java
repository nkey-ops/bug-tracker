package com.bluesky.bugtraker.view.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {


    @GetMapping("/login")
    public String viewLoginPage() {
        return "login";
    }

    @RequestMapping(value = {"/login?error", "/loginFailure"})
    public String getError() {
        return "401";
    }


//    @PostMapping ("/login")
//    public String postLoginPage() {
//        return "index";
//    }

    @RequestMapping("/home")
    public  String home(){
        return  "index";
    }
}
