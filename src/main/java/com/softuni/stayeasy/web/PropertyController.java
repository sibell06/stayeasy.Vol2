package com.softuni.stayeasy.web;

import com.softuni.stayeasy.model.dto.property.PropertyBindingModel;
import com.softuni.stayeasy.model.dto.review.ReviewBindingModel;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.property.PropertyType;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.security.UserPrincipal;
import com.softuni.stayeasy.service.property.PropertyService;
import com.softuni.stayeasy.service.review.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final ReviewService reviewService;

    public PropertyController(PropertyService propertyService, ReviewService reviewService) {
        this.propertyService = propertyService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String browse(Model model) {
        model.addAttribute("properties", propertyService.findAllAvailable());
        return "property/browse";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id,
                          @RequestParam(required = false) String alreadyReviewed,
                          @RequestParam(required = false) String ratingError,
                          Model model,
                          @AuthenticationPrincipal UserPrincipal principal) {
        Optional<Property> propertyOpt = propertyService.findById(id);

        if (propertyOpt.isEmpty()) {
            return "redirect:/properties";
        }

        Property property = propertyOpt.get();
        model.addAttribute("property", property);
        model.addAttribute("reviews", reviewService.findAllByProperty(property));
        model.addAttribute("reviewData", new ReviewBindingModel());
        model.addAttribute("alreadyReviewed", alreadyReviewed != null);
        model.addAttribute("ratingError", ratingError != null);

        if (principal != null) {
            model.addAttribute("userAlreadyReviewed",
                    reviewService.hasUserReviewedProperty(principal.getUser(), property));
        }

        return "property/details";
    }

    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("propertyData", new PropertyBindingModel());
        model.addAttribute("propertyTypes", PropertyType.values());
        return "property/add";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("propertyData") PropertyBindingModel propertyData,
                      BindingResult bindingResult,
                      Model model,
                      @AuthenticationPrincipal UserPrincipal principal) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("propertyTypes", PropertyType.values());
            return "property/add";
        }

        Property property = Property.builder()
                .title(propertyData.getTitle())
                .description(propertyData.getDescription())
                .location(propertyData.getLocation())
                .pricePerNight(propertyData.getPricePerNight())
                .maxGuest(propertyData.getMaxGuest())
                .bedrooms(propertyData.getBedrooms())
                .bathrooms(propertyData.getBathrooms())
                .imageUrl(propertyData.getImageUrl())
                .type(propertyData.getType())
                .host(principal.getUser())
                .build();

        propertyService.createProperty(property);
        return "redirect:/properties";
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable UUID id,
                           Model model,
                           @AuthenticationPrincipal UserPrincipal principal) {

        Optional<Property> propertyOpt = propertyService.findById(id);

        if (propertyOpt.isEmpty()) {
            return "redirect:/properties";
        }

        Property property = propertyOpt.get();
        boolean isOwner = property.getHost().getId().equals(principal.getId());
        boolean isAdmin = principal.getUser().getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/properties";
        }

        PropertyBindingModel propertyData = new PropertyBindingModel();
        propertyData.setTitle(property.getTitle());
        propertyData.setDescription(property.getDescription());
        propertyData.setLocation(property.getLocation());
        propertyData.setPricePerNight(property.getPricePerNight());
        propertyData.setMaxGuest(property.getMaxGuest());
        propertyData.setBedrooms(property.getBedrooms());
        propertyData.setBathrooms(property.getBathrooms());
        propertyData.setImageUrl(property.getImageUrl());
        propertyData.setType(property.getType());

        model.addAttribute("propertyData", propertyData);
        model.addAttribute("propertyTypes", PropertyType.values());
        model.addAttribute("propertyId", id);
        return "property/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable UUID id,
                       @Valid @ModelAttribute("propertyData") PropertyBindingModel propertyData,
                       BindingResult bindingResult,
                       Model model,
                       @AuthenticationPrincipal UserPrincipal principal) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("propertyTypes", PropertyType.values());
            model.addAttribute("propertyId", id);
            return "property/edit";
        }

        Optional<Property> propertyOpt = propertyService.findById(id);
        if (propertyOpt.isEmpty()) {
            return "redirect:/properties";
        }

        Property property = propertyOpt.get();
        boolean isOwner = property.getHost().getId().equals(principal.getId());
        boolean isAdmin = principal.getUser().getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/properties";
        }

        property.setTitle(propertyData.getTitle());
        property.setDescription(propertyData.getDescription());
        property.setLocation(propertyData.getLocation());
        property.setPricePerNight(propertyData.getPricePerNight());
        property.setMaxGuest(propertyData.getMaxGuest());
        property.setBedrooms(propertyData.getBedrooms());
        property.setBathrooms(propertyData.getBathrooms());
        property.setImageUrl(propertyData.getImageUrl());
        property.setType(propertyData.getType());

        propertyService.updateProperty(property);
        return "redirect:/properties/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id,
                         @AuthenticationPrincipal UserPrincipal principal) {

        Optional<Property> propertyOpt = propertyService.findById(id);
        if (propertyOpt.isEmpty()) {
            return "redirect:/properties";
        }

        Property property = propertyOpt.get();
        boolean isOwner = property.getHost().getId().equals(principal.getId());
        boolean isAdmin = principal.getUser().getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/properties";
        }

        propertyService.deleteProperty(id);
        return "redirect:/properties";
    }
}