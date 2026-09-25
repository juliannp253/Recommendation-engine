package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.config.SecurityConfig;
import com.project.recommendation_engine.model.TMDBResponse;
import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.repository.UserRepository;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
@Import(SecurityConfig.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TMDBService tmdbService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    @WithMockUser
    void searchPage_authenticatedUser_shouldRenderSearchView() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("search"));
    }

    @Test
    void searchPage_unauthenticatedUser_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void searchMovies_movieFound_shouldPopulateModelAndReturnSearchView() throws Exception {
        TMDBResponse movieResponse = new TMDBResponse();
        movieResponse.setTitle("Inception");
        movieResponse.setResponse("True");

        when(tmdbService.fetchRawMovieResponse("Inception")).thenReturn(movieResponse);

        mockMvc.perform(post("/search")
                        .param("movieTitle", "Inception")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("searchQuery", "Inception"))
                .andExpect(model().attribute("searchResults", "found"))
                .andExpect(model().attribute("movie", movieResponse));

        verify(tmdbService).fetchRawMovieResponse("Inception");
    }

    @Test
    @WithMockUser
    void searchMovies_movieNotFound_shouldSetNoResultsMessage() throws Exception {
        when(tmdbService.fetchRawMovieResponse("Unknown Movie")).thenReturn(null);

        mockMvc.perform(post("/search")
                        .param("movieTitle", "Unknown Movie")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attribute("searchQuery", "Unknown Movie"))
                .andExpect(model().attribute("searchResults", "No results found for: Unknown Movie"))
                .andExpect(model().attributeDoesNotExist("movie"));

        verify(tmdbService).fetchRawMovieResponse("Unknown Movie");
    }

    @Test
    @WithMockUser(username = "alice@example.com")
    void rateMovie_validRequest_shouldSaveRatingAndReturnOk() throws Exception {
        User user = new User();
        user.setId("user-123");
        user.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/search/rate-movie")
                        .param("movieId", "550")
                        .param("rating", "4.5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Rating saved successfully"));

        verify(userService).addOrUpdateRating("user-123", "550", 4.5);
    }

    @Test
    @WithMockUser(username = "missing@example.com")
    void rateMovie_userNotFound_shouldReturnInternalServerError() throws Exception {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/search/rate-movie")
                        .param("movieId", "550")
                        .param("rating", "4.5")
                        .with(csrf()))
                .andExpect(status().is5xxServerError())
                .andExpect(content().string("Error saving rating"));

        verify(userService, never()).addOrUpdateRating(anyString(), anyString(), anyDouble());
    }
}
