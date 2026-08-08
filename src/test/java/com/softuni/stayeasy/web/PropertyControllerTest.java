package com.softuni.stayeasy.web;

import com.softuni.stayeasy.config.SecurityConfiguration;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.security.CustomUserDetailsService;
import com.softuni.stayeasy.security.UserPrincipal;
import com.softuni.stayeasy.service.property.PropertyService;
import com.softuni.stayeasy.service.review.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyController.class)
@Import(SecurityConfiguration.class)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PropertyService propertyService;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void browse_anonymousUser_returnsOkAndBrowseView() throws Exception {
        when(propertyService.findAllAvailable()).thenReturn(List.of());

        mockMvc.perform(get("/properties"))
                .andExpect(status().isOk())
                .andExpect(view().name("property/browse"));
    }

    @Test
    void addPage_anonymousUser_isRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/properties/add"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void addPage_authenticatedHost_returnsOkAndAddView() throws Exception {
        User host = User.builder().id(UUID.randomUUID()).username("host1").password("x").role(UserRole.HOST).build();
        UserPrincipal principal = new UserPrincipal(host);

        mockMvc.perform(get("/properties/add").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(view().name("property/add"));
    }

    @Test
    void deleteProperty_ownerHost_redirectsToProperties() throws Exception {
        User host = User.builder().id(UUID.randomUUID()).username("host1").password("x").role(UserRole.HOST).build();
        UserPrincipal principal = new UserPrincipal(host);

        Property property = Property.builder()
                .id(UUID.randomUUID())
                .title("Test Property")
                .host(host)
                .build();

        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(post("/properties/{id}/delete", property.getId())
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));
    }
}