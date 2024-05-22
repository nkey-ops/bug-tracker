package com.bluesky.bugtraker.view.controller;

import com.bluesky.bugtraker.view.model.request.UserRegisterModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

  @GetMapping("/signup")
  public String signUp(Model model) {
    model.addAttribute("userRegisterModel", new UserRegisterModel());

    return "pages/register";
  }

  @GetMapping("/login")
  public String viewLoginPage() {
    return "pages/login";
  }

  @GetMapping("/logout")
  public String logout() {
    return "pages/login";
  }

  @GetMapping("/verification/email")
  public String emailVerification() {
    return "pages/email-verification";
  }
}
