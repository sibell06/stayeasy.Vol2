package com.softuni.stayeasy.config;

import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.property.PropertyType;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.repository.property.PropertyRepository;
import com.softuni.stayeasy.repository.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PropertyRepository propertyRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser("admin1", "admin1@stayeasy.com", "Admin123!", "Admin", "One", UserRole.ADMIN);
        User host = seedUser("host1", "host1@stayeasy.com", "Host123!", "Host", "One", UserRole.HOST);
        seedUser("renter1", "renter1@stayeasy.com", "Renter123!", "Renter", "One", UserRole.RENTER);

        if (host != null && propertyRepository.findAllByHost(host).isEmpty()) {
            seedProperty(host, "Sunny Downtown Apartment", "A bright, modern apartment in the city center.",
                    "Sofia, Bulgaria", new BigDecimal("65.00"), 4, 2, 1, PropertyType.APARTMENT);

            seedProperty(host, "Cozy Mountain Villa", "A peaceful villa with mountain views, perfect for a getaway.",
                    "Sarnitsa, Bulgaria", new BigDecimal("120.00"), 6, 3, 2, PropertyType.VILLA);
        }
    }

    private User seedUser(String username, String email, String rawPassword, String firstName, String lastName, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            return userRepository.findByUsername(username).orElse(null);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    private void seedProperty(User host, String title, String description, String location,
                              BigDecimal pricePerNight, int maxGuest, int bedrooms, int bathrooms,
                              PropertyType type) {

        Property property = Property.builder()
                .title(title)
                .description(description)
                .location(location)
                .pricePerNight(pricePerNight)
                .maxGuest(maxGuest)
                .bedrooms(bedrooms)
                .bathrooms(bathrooms)
                .type(type)
                .available(true)
                .host(host)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        propertyRepository.save(property);
    }
}