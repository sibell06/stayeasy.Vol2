package com.softuni.stayeasy.web;

import com.softuni.stayeasy.config.SecurityConfiguration;
import com.softuni.stayeasy.model.dto.user.RegisterBindingModel;
import com.softuni.stayeasy.security.CustomUserDetailsService;
import com.softuni.stayeasy.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void registerPage_returnsOkAndRegisterView() throws Exception {
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void register_validData_redirectsToLogin() throws Exception {
        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("newuser@stayeasy.com")).thenReturn(false);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@stayeasy.com")
                        .param("password", "Password123!")
                        .param("confirmPassword", "Password123!")
                        .param("firstName", "New")
                        .param("lastName", "User"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void register_passwordMismatch_returnsRegisterViewWithError() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("email", "newuser@stayeasy.com")
                        .param("password", "Password123!")
                        .param("confirmPassword", "DifferentPassword!")
                        .param("firstName", "New")
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attribute("passwordMismatch", true));
    }

    @Test
    void register_usernameTaken_returnsRegisterViewWithError() throws Exception {
        when(userService.existsByUsername("existinguser")).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .param("username", "existinguser")
                        .param("email", "new@stayeasy.com")
                        .param("password", "Password123!")
                        .param("confirmPassword", "Password123!")
                        .param("firstName", "New")
                        .param("lastName", "User"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("usernameTaken", true));
    }

    @Test
    void loginPage_noError_returnsOkWithoutInvalidCredentialsFlag() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("invalidCredentials", false));
    }

    @Test
    void loginPage_withError_returnsOkWithInvalidCredentialsFlag() throws Exception {
        mockMvc.perform(get("/auth/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("invalidCredentials", true));
    }
}