package com.softuni.stayeasy.web;

import com.softuni.stayeasy.config.SecurityConfiguration;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.property.PropertyType;
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

import java.math.BigDecimal;
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

    private User host;
    private UserPrincipal hostPrincipal;
    private Property property;

    private void setUp() {
        host = User.builder().id(UUID.randomUUID()).username("host1").password("x").role(UserRole.HOST).build();
        hostPrincipal = new UserPrincipal(host);
        property = Property.builder()
                .id(UUID.randomUUID())
                .title("Test Property")
                .host(host)
                .pricePerNight(new BigDecimal("50.00"))
                .maxGuest(4)
                .build();
    }

    @Test
    void browse_anonymousUser_returnsOkAndBrowseView() throws Exception {
        when(propertyService.findAllAvailable()).thenReturn(List.of());

        mockMvc.perform(get("/properties"))
                .andExpect(status().isOk())
                .andExpect(view().name("property/browse"));
    }

    @Test
    void details_existingProperty_returnsOkAndDetailsView() throws Exception {
        setUp();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));
        when(reviewService.findAllByProperty(property)).thenReturn(List.of());

        mockMvc.perform(get("/properties/{id}", property.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("property/details"));
    }

    @Test
    void details_nonExistentProperty_redirectsToProperties() throws Exception {
        UUID randomId = UUID.randomUUID();
        when(propertyService.findById(randomId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/properties/{id}", randomId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));
    }

    @Test
    void addPage_anonymousUser_isRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/properties/add"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void addPage_authenticatedHost_returnsOkAndAddView() throws Exception {
        setUp();
        mockMvc.perform(get("/properties/add").with(user(hostPrincipal)))
                .andExpect(status().isOk())
                .andExpect(view().name("property/add"));
    }

    @Test
    void add_validData_redirectsToProperties() throws Exception {
        setUp();

        mockMvc.perform(post("/properties/add")
                        .with(user(hostPrincipal))
                        .with(csrf())
                        .param("title", "New Property")
                        .param("description", "A lovely place to stay for a while")
                        .param("location", "Sofia")
                        .param("pricePerNight", "60.00")
                        .param("maxGuest", "3")
                        .param("bedrooms", "1")
                        .param("bathrooms", "1")
                        .param("type", PropertyType.APARTMENT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));
    }

    @Test
    void editPage_ownerHost_returnsOkAndEditView() throws Exception {
        setUp();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(get("/properties/{id}/edit", property.getId()).with(user(hostPrincipal)))
                .andExpect(status().isOk())
                .andExpect(view().name("property/edit"));
    }

    @Test
    void editPage_nonOwner_redirectsToProperties() throws Exception {
        setUp();
        User otherHost = User.builder().id(UUID.randomUUID()).username("host2").password("x").role(UserRole.HOST).build();
        UserPrincipal otherPrincipal = new UserPrincipal(otherHost);
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(get("/properties/{id}/edit", property.getId()).with(user(otherPrincipal)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));
    }

    @Test
    void edit_ownerHost_redirectsToPropertyDetails() throws Exception {
        setUp();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(post("/properties/{id}/edit", property.getId())
                        .with(user(hostPrincipal))
                        .with(csrf())
                        .param("title", "Updated Property")
                        .param("description", "An updated lovely place to stay")
                        .param("location", "Sofia")
                        .param("pricePerNight", "70.00")
                        .param("maxGuest", "3")
                        .param("bedrooms", "1")
                        .param("bathrooms", "1")
                        .param("type", PropertyType.APARTMENT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties/" + property.getId()));
    }

    @Test
    void deleteProperty_ownerHost_redirectsToProperties() throws Exception {
        setUp();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(post("/properties/{id}/delete", property.getId())
                        .with(user(hostPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));
    }

    @Test
    void deleteProperty_nonOwner_redirectsWithoutDeleting() throws Exception {
        setUp();
        User otherHost = User.builder().id(UUID.randomUUID()).username("host2").password("x").role(UserRole.HOST).build();
        UserPrincipal otherPrincipal = new UserPrincipal(otherHost);
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(post("/properties/{id}/delete", property.getId())
                        .with(user(otherPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties"));
    }
}