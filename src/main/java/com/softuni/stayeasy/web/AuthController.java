package com.softuni.stayeasy.web;

import com.softuni.stayeasy.model.dto.user.LoginBindingModel;
import com.softuni.stayeasy.model.dto.user.RegisterBindingModel;
import com.softuni.stayeasy.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerData", new RegisterBindingModel());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerData") RegisterBindingModel registerData,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!registerData.getPassword().equals(registerData.getConfirmPassword())) {
            model.addAttribute("registerData", registerData);
            model.addAttribute("passwordMismatch", true);
            return "auth/register";
        }

        if (userService.existsByUsername(registerData.getUsername())) {
            model.addAttribute("registerData", registerData);
            model.addAttribute("usernameTaken", true);
            return "auth/register";
        }

        if (userService.existsByEmail(registerData.getEmail())) {
            model.addAttribute("registerData", registerData);
            model.addAttribute("emailTaken", true);
            return "auth/register";
        }

        userService.register(
                registerData.getUsername(),
                registerData.getEmail(),
                registerData.getPassword(),
                registerData.getFirstName(),
                registerData.getLastName()
        );

        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("loginData", new LoginBindingModel());
        model.addAttribute("invalidCredentials", error != null);
        return "auth/login";
    }
}