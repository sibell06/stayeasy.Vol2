package com.softuni.stayeasy.service.property;

import com.softuni.stayeasy.exception.PropertyNotFoundException;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.repository.property.PropertyRepository;
import com.softuni.stayeasy.repository.reservation.ReservationRepository;
import com.softuni.stayeasy.repository.review.ReviewRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    public PropertyServiceImpl(PropertyRepository propertyRepository,
                               ReviewRepository reviewRepository,
                               ReservationRepository reservationRepository) {
        this.propertyRepository = propertyRepository;
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @CacheEvict(value = "availableProperties", allEntries = true)
    public void createProperty(Property property) {
        property.setCreatedOn(LocalDateTime.now());
        property.setUpdatedOn(LocalDateTime.now());
        property.setAvailable(true);
        propertyRepository.save(property);
    }

    @Override
    @CacheEvict(value = "availableProperties", allEntries = true)
    public void updateProperty(Property property) {
        property.setUpdatedOn(LocalDateTime.now());
        propertyRepository.save(property);
    }

    @Override
    @Transactional
    @CacheEvict(value = "availableProperties", allEntries = true)
    public void deleteProperty(UUID id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property with id " + id + " not found"));

        reviewRepository.deleteAll(reviewRepository.findAllByProperty(property));
        reservationRepository.deleteAll(reservationRepository.findAllByProperty(property));
        propertyRepository.delete(property);
    }

    @Override
    public Optional<Property> findById(UUID id) {
        return propertyRepository.findById(id);
    }

    @Override
    public Property findByIdOrThrow(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException("Property with id " + id + " not found"));
    }

    @Override
    @Cacheable("availableProperties")
    public List<Property> findAllAvailable() {
        return propertyRepository.findAllByAvailableTrue();
    }

    @Override
    public List<Property> findAllByHost(User host) {
        return propertyRepository.findAllByHost(host);
    }
}