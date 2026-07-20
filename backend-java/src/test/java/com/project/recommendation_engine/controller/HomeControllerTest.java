package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)          // Import SecurityConfig to access endpoint without authentication
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homePageShouldReturnView() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())       // Response HTTP 200
                .andExpect(view().name("home"));

    }
}