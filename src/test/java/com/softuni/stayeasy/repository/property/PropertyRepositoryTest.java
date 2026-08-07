package com.softuni.stayeasy.repository.property;

import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.property.PropertyType;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    private User host;

    @BeforeEach
    void setUp() {
        host = User.builder()
                .username("host1")
                .password("hashed")
                .email("host1@stayeasy.com")
                .role(UserRole.HOST)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        host = userRepository.save(host);
    }

    @Test
    void save_and_findById_returnsCorrectProperty() {
        Property property = buildProperty(true);
        Property saved = propertyRepository.save(property);

        var found = propertyRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Cozy Apartment", found.get().getTitle());
        assertEquals(host.getId(), found.get().getHost().getId());
    }

    @Test
    void findAllByAvailableTrue_returnsOnlyAvailableProperties() {
        propertyRepository.save(buildProperty(true));
        propertyRepository.save(buildProperty(false));

        List<Property> result = propertyRepository.findAllByAvailableTrue();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
    }

    @Test
    void findAllByHost_returnsOnlyThatHostsProperties() {
        propertyRepository.save(buildProperty(true));

        User otherHost = userRepository.save(User.builder()
                .username("host2")
                .password("hashed")
                .email("host2@stayeasy.com")
                .role(UserRole.HOST)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build());

        Property otherProperty = Property.builder()
                .title("Other Place")
                .description("Another property")
                .location("Varna")
                .pricePerNight(new BigDecimal("40.00"))
                .maxGuest(4)
                .bedrooms(2)
                .bathrooms(1)
                .type(PropertyType.HOUSE)
                .available(true)
                .host(otherHost)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        propertyRepository.save(otherProperty);

        List<Property> result = propertyRepository.findAllByHost(host);

        assertEquals(1, result.size());
        assertEquals(host.getId(), result.get(0).getHost().getId());
    }

    private Property buildProperty(boolean available) {
        return Property.builder()
                .title("Cozy Apartment")
                .description("A nice place to stay")
                .location("Sofia")
                .pricePerNight(new BigDecimal("50.00"))
                .maxGuest(2)
                .bedrooms(1)
                .bathrooms(1)
                .type(PropertyType.APARTMENT)
                .available(available)
                .host(host)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}