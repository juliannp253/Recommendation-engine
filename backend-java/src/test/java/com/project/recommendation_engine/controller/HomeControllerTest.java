package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.config.SecurityConfig;
import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.RecommendationRepository;
import com.project.recommendation_engine.repository.UserRepository;
import com.project.recommendation_engine.service.RecommendationAgentService;
import com.project.recommendation_engine.service.TMDBService;
import com.project.recommendation_engine.service.UserDetailsServiceImpl;
import com.project.recommendation_engine.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TMDBService tmdbService;
    @MockBean
    private UserService userService;
    @MockBean
    private RecommendationRepository recommendationRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RecommendationAgentService agentService;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private BCryptPasswordEncoder passwordEncoder;


    @Test
    @WithMockUser
    void homePageShouldReturnView() throws Exception {
        User mockUSer = new User();
        mockUSer.setId("abc123");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUSer));
        when(tmdbService.fetchTrendingMovies()).thenReturn(List.of());
        when(recommendationRepository.findFirstByUserIdOrderByGeneratedAtDesc(anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));

    }
}