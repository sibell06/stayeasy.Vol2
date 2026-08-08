package com.softuni.stayeasy.web;

import com.softuni.stayeasy.client.LoyaltyServiceClient;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfiguration.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private LoyaltyServiceClient loyaltyServiceClient;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void view_authenticatedUser_returnsOkWithBalance() throws Exception {
        User renter = User.builder().id(UUID.randomUUID()).username("renter1").password("x").role(UserRole.RENTER).build();
        UserPrincipal principal = new UserPrincipal(renter);
        when(loyaltyServiceClient.getBalance(renter.getId())).thenReturn(Map.of("pointsBalance", 40));

        mockMvc.perform(get("/profile").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/view"))
                .andExpect(model().attribute("pointsBalance", 40));
    }

    @Test
    void view_anonymousUser_isRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void editPage_authenticatedUser_returnsOkWithPrefilledData() throws Exception {
        User renter = User.builder().id(UUID.randomUUID()).username("renter1").password("x")
                .role(UserRole.RENTER).firstName("Renter").lastName("One").build();
        UserPrincipal principal = new UserPrincipal(renter);

        mockMvc.perform(get("/profile/edit").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/edit"));
    }

    @Test
    void edit_validData_redirectsToProfile() throws Exception {
        User renter = User.builder().id(UUID.randomUUID()).username("renter1").password("x").role(UserRole.RENTER).build();
        UserPrincipal principal = new UserPrincipal(renter);

        mockMvc.perform(post("/profile/edit")
                        .with(user(principal))
                        .with(csrf())
                        .param("firstName", "Updated")
                        .param("lastName", "Name")
                        .param("phoneNumber", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    @Test
    void edit_invalidData_returnsEditView() throws Exception {
        User renter = User.builder().id(UUID.randomUUID()).username("renter1").password("x").role(UserRole.RENTER).build();
        UserPrincipal principal = new UserPrincipal(renter);

        mockMvc.perform(post("/profile/edit")
                        .with(user(principal))
                        .with(csrf())
                        .param("firstName", "")
                        .param("lastName", "Name"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile/edit"));
    }
}