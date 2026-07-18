package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.repository.UserRepository;
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
    private final UserRepository userRepository;

    public ProfileController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showProfile(Model model, Authentication authentication) {
        String currentUsername = authentication.getName();
        User user = userRepository.findByEmail(currentUsername).orElseThrow(() -> new RuntimeException("User Not Found"));

        model.addAttribute("email", user.getEmail());
        model.addAttribute("favoriteGenres", user.getFavoriteGenres());

        return "profile";
    }
}
