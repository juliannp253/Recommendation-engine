package com.project.recommendation_engine.controller;

import com.project.recommendation_engine.config.SecurityConfig;
import com.project.recommendation_engine.model.User;
import com.project.recommendation_engine.service.UserDetailsServiceImpl;
import com.project.recommendation_engine.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@WebMvcTest(RegisterController.class)
@Import(SecurityConfig.class)
class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private BCryptPasswordEncoder passwordEncoder;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldRenderRegisterForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void shouldRegisterUserAndRedirectToHome() throws Exception {
        User savedUser = new User();
        savedUser.setId("abc123");
        when(userService.registerUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/register")
                        .param("email", "user@example.com")
                        .param("password", "1234")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/questionnaire?userId=*"));
    }

    @Test
    void shouldShowErrorIfEmailExists() throws Exception {
        doThrow(new RuntimeException("This email already exists."))
                .when(userService).registerUser(any(User.class));

        mockMvc.perform(post("/register")
                        .param("username", "julian")
                        .param("email", "julian@example.com")
                        .param("password", "secret")
                        .param("confirmPassword", "secret")
                        .with(csrf()))
                .andExpect(status().isOk()) // Return to form
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registrationError"))
                .andExpect(model().attribute("registrationError", "This email already exists."));

        verify(userService).registerUser(any(User.class));
    }

}
