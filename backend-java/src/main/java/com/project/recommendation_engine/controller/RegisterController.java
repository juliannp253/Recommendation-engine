package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegisterController {
    private final UserService userService;


    @Autowired
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    // GET → Render view
    @GetMapping
    public String registerForm() {
        return "register";
    }

    // POST → Process form
    @PostMapping
    public String registerUser(@ModelAttribute User user, Model model) {
        User savedUser;
        
        try {
            savedUser = userService.registerUser(user);
        } catch (RuntimeException e) {
            model.addAttribute("registrationError", e.getMessage());
            return "register"; // Show form again
        }
        String userId = savedUser.getId();

        return "redirect:/questionnaire?userId=" + userId; // Continue to Home Page
    }
}

