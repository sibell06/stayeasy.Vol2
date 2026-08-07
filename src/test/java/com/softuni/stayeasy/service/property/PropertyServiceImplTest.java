package com.softuni.stayeasy.service.property;

import com.softuni.stayeasy.exception.PropertyNotFoundException;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.property.PropertyType;
import com.softuni.stayeasy.model.entity.reservation.Reservation;
import com.softuni.stayeasy.model.entity.review.Review;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.repository.property.PropertyRepository;
import com.softuni.stayeasy.repository.reservation.ReservationRepository;
import com.softuni.stayeasy.repository.review.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private User host;
    private Property property;
    private UUID propertyId;

    @BeforeEach
    void setUp() {
        propertyId = UUID.randomUUID();
        host = User.builder().id(UUID.randomUUID()).build();
        property = Property.builder()
                .id(propertyId)
                .title("Cozy Apartment")
                .description("A nice place")
                .location("Sofia")
                .pricePerNight(new BigDecimal("50.00"))
                .maxGuest(2)
                .bedrooms(1)
                .bathrooms(1)
                .type(PropertyType.APARTMENT)
                .host(host)
                .build();
    }

    @Test
    void createProperty_setsDefaultsAndSaves() {
        propertyService.createProperty(property);

        assertTrue(property.isAvailable());
        assertNotNull(property.getCreatedOn());
        assertNotNull(property.getUpdatedOn());
        verify(propertyRepository, times(1)).save(property);
    }

    @Test
    void updateProperty_updatesTimestampAndSaves() {
        propertyService.updateProperty(property);

        assertNotNull(property.getUpdatedOn());
        verify(propertyRepository, times(1)).save(property);
    }

    @Test
    void deleteProperty_existingProperty_deletesReviewsReservationsAndProperty() {
        List<Review> reviews = List.of(Review.builder().id(UUID.randomUUID()).build());
        List<Reservation> reservations = List.of(Reservation.builder().id(UUID.randomUUID()).build());

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(reviewRepository.findAllByProperty(property)).thenReturn(reviews);
        when(reservationRepository.findAllByProperty(property)).thenReturn(reservations);

        propertyService.deleteProperty(propertyId);

        verify(reviewRepository, times(1)).deleteAll(reviews);
        verify(reservationRepository, times(1)).deleteAll(reservations);
        verify(propertyRepository, times(1)).delete(property);
    }

    @Test
    void deleteProperty_nonExistentProperty_throwsException() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.deleteProperty(propertyId));

        verify(propertyRepository, never()).delete(any());
    }

    @Test
    void findById_existingProperty_returnsProperty() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

        Optional<Property> found = propertyService.findById(propertyId);

        assertTrue(found.isPresent());
        assertEquals("Cozy Apartment", found.get().getTitle());
    }

    @Test
    void findByIdOrThrow_nonExistentProperty_throwsException() {
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThrows(PropertyNotFoundException.class, () -> propertyService.findByIdOrThrow(propertyId));
    }

    @Test
    void findAllAvailable_returnsAvailableProperties() {
        when(propertyRepository.findAllByAvailableTrue()).thenReturn(List.of(property));

        List<Property> result = propertyService.findAllAvailable();

        assertEquals(1, result.size());
    }

    @Test
    void findAllByHost_returnsHostProperties() {
        when(propertyRepository.findAllByHost(host)).thenReturn(List.of(property));

        List<Property> result = propertyService.findAllByHost(host);

        assertEquals(1, result.size());
        verify(propertyRepository, times(1)).findAllByHost(host);
    }
}