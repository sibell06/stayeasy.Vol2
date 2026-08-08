package com.softuni.stayeasy.web;

import com.softuni.stayeasy.config.SecurityConfiguration;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.review.Review;
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

@WebMvcTest(ReviewController.class)
@Import(SecurityConfiguration.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private PropertyService propertyService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private User author;
    private User host;
    private Property property;
    private UserPrincipal authorPrincipal;

    private void setUp() {
        author = User.builder().id(UUID.randomUUID()).username("renter1").password("x").role(UserRole.RENTER).build();
        host = User.builder().id(UUID.randomUUID()).username("host1").password("x").role(UserRole.HOST).build();
        property = Property.builder().id(UUID.randomUUID()).title("Test Property").host(host).build();
        authorPrincipal = new UserPrincipal(author);
    }

    @Test
    void create_validReview_redirectsToPropertyDetails() throws Exception {
        setUp();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));
        when(reviewService.hasUserReviewedProperty(author, property)).thenReturn(false);

        mockMvc.perform(post("/reviews/create/{propertyId}", property.getId())
                        .with(user(authorPrincipal))
                        .with(csrf())
                        .param("rating", "5")
                        .param("content", "Wonderful stay, highly recommend it!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties/" + property.getId()));
    }

    @Test
    void create_alreadyReviewed_redirectsWithFlag() throws Exception {
        setUp();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));
        when(reviewService.hasUserReviewedProperty(author, property)).thenReturn(true);

        mockMvc.perform(post("/reviews/create/{propertyId}", property.getId())
                        .with(user(authorPrincipal))
                        .with(csrf())
                        .param("rating", "5")
                        .param("content", "Wonderful stay, highly recommend it!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties/" + property.getId() + "?alreadyReviewed=true"));
    }

    @Test
    void create_invalidRating_returnsPropertyDetailsView() throws Exception {
        setUp();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));
        when(reviewService.findAllByProperty(property)).thenReturn(List.of());
        when(reviewService.hasUserReviewedProperty(author, property)).thenReturn(false);

        mockMvc.perform(post("/reviews/create/{propertyId}", property.getId())
                        .with(user(authorPrincipal))
                        .with(csrf())
                        .param("rating", "0")
                        .param("content", "Too short review"))
                .andExpect(status().isOk())
                .andExpect(view().name("property/details"));
    }

    @Test
    void delete_ownerAuthor_redirectsToPropertyDetails() throws Exception {
        setUp();
        Review review = Review.builder().id(UUID.randomUUID()).author(author).property(property).build();
        when(reviewService.findById(review.getId())).thenReturn(Optional.of(review));

        mockMvc.perform(post("/reviews/delete/{reviewId}", review.getId())
                        .with(user(authorPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties/" + property.getId()));
    }

    @Test
    void delete_nonOwner_redirectsWithoutDeleting() throws Exception {
        setUp();
        User otherUser = User.builder().id(UUID.randomUUID()).username("other").password("x").role(UserRole.RENTER).build();
        Review review = Review.builder().id(UUID.randomUUID()).author(otherUser).property(property).build();
        when(reviewService.findById(review.getId())).thenReturn(Optional.of(review));

        mockMvc.perform(post("/reviews/delete/{reviewId}", review.getId())
                        .with(user(authorPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/properties/" + property.getId()));
    }
}