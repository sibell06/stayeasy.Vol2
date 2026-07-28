package com.softuni.stayeasy.web;

import com.softuni.stayeasy.model.dto.review.ReviewBindingModel;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.review.Review;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.security.UserPrincipal;
import com.softuni.stayeasy.service.property.PropertyService;
import com.softuni.stayeasy.service.review.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final PropertyService propertyService;

    public ReviewController(ReviewService reviewService, PropertyService propertyService) {
        this.reviewService = reviewService;
        this.propertyService = propertyService;
    }

    @PostMapping("/create/{propertyId}")
    public String create(@PathVariable UUID propertyId,
                         @Valid @ModelAttribute("reviewData") ReviewBindingModel reviewData,
                         BindingResult bindingResult,
                         Model model,
                         @AuthenticationPrincipal UserPrincipal principal) {

        Optional<Property> propertyOpt = propertyService.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return "redirect:/properties";
        }

        Property property = propertyOpt.get();
        User author = principal.getUser();

        if (bindingResult.hasErrors()) {
            model.addAttribute("property", property);
            model.addAttribute("reviews", reviewService.findAllByProperty(property));
            model.addAttribute("reviewData", reviewData);
            model.addAttribute("userAlreadyReviewed", reviewService.hasUserReviewedProperty(author, property));
            return "property/details";
        }

        if (reviewService.hasUserReviewedProperty(author, property)) {
            return "redirect:/properties/" + propertyId + "?alreadyReviewed=true";
        }

        Review review = Review.builder()
                .content(reviewData.getContent())
                .rating(reviewData.getRating())
                .author(author)
                .property(property)
                .build();

        reviewService.createReview(review);
        return "redirect:/properties/" + propertyId;
    }

    @PostMapping("/delete/{reviewId}")
    public String delete(@PathVariable UUID reviewId, @AuthenticationPrincipal UserPrincipal principal) {
        Optional<Review> reviewOpt = reviewService.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            return "redirect:/properties";
        }

        boolean isOwner = reviewOpt.get().getAuthor().getId().equals(principal.getId());
        boolean isAdmin = principal.getUser().getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/properties/" + reviewOpt.get().getProperty().getId();
        }

        UUID propertyId = reviewOpt.get().getProperty().getId();
        reviewService.deleteReview(reviewId);
        return "redirect:/properties/" + propertyId;
    }
}