package com.project.recommendation_engine.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        
        // Fetch user from database
        User user = userService.findByUsername(currentUsername);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        // Add user data to model
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("favoriteGenres", user.getFavoriteGenres());
        
        return "profile";
    }
}
