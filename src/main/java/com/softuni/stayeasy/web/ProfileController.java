package com.softuni.stayeasy.web;

import com.softuni.stayeasy.model.dto.user.ProfileEditBindingModel;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.security.UserPrincipal;
import com.softuni.stayeasy.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String view(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("profileUser", principal.getUser());
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editPage(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();

        ProfileEditBindingModel profileData = new ProfileEditBindingModel();
        profileData.setFirstName(user.getFirstName());
        profileData.setLastName(user.getLastName());
        profileData.setPhoneNumber(user.getPhoneNumber());
        profileData.setProfilePicture(user.getProfilePicture());

        model.addAttribute("profileData", profileData);
        return "profile/edit";
    }

    @PostMapping("/edit")
    public String edit(@Valid @ModelAttribute("profileData") ProfileEditBindingModel profileData,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal UserPrincipal principal) {

        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }

        User user = principal.getUser();
        user.setFirstName(profileData.getFirstName());
        user.setLastName(profileData.getLastName());
        user.setPhoneNumber(profileData.getPhoneNumber());
        user.setProfilePicture(profileData.getProfilePicture());

        userService.updateUser(user);
        return "redirect:/profile";
    }
}