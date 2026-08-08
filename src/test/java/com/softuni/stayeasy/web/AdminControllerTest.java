package com.softuni.stayeasy.web;

import com.softuni.stayeasy.config.SecurityConfiguration;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.security.CustomUserDetailsService;
import com.softuni.stayeasy.security.UserPrincipal;
import com.softuni.stayeasy.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfiguration.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void users_asAdmin_returnsOkWithUsersView() throws Exception {
        User admin = User.builder().id(UUID.randomUUID()).username("admin1").password("x").role(UserRole.ADMIN).build();
        UserPrincipal principal = new UserPrincipal(admin);
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"));
    }

    @Test
    void users_asNonAdmin_isForbidden() throws Exception {
        User renter = User.builder().id(UUID.randomUUID()).username("renter1").password("x").role(UserRole.RENTER).build();
        UserPrincipal principal = new UserPrincipal(renter);

        mockMvc.perform(get("/admin/users").with(user(principal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeRole_asAdmin_redirectsToUsers() throws Exception {
        User admin = User.builder().id(UUID.randomUUID()).username("admin1").password("x").role(UserRole.ADMIN).build();
        UserPrincipal principal = new UserPrincipal(admin);
        UUID targetUserId = UUID.randomUUID();

        mockMvc.perform(post("/admin/users/{id}/role", targetUserId)
                        .with(user(principal))
                        .with(csrf())
                        .param("role", "HOST"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }
}