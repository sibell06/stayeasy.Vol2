package com.softuni.stayeasy.service.review;

import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.review.Review;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.repository.review.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void createReview(Review review) {
        review.setCreatedOn(LocalDateTime.now());
        reviewRepository.save(review);
        logger.info("Review created by user {} for property {} (rating: {})",
                review.getAuthor().getId(), review.getProperty().getId(), review.getRating());
    }

    @Override
    public void deleteReview(UUID id) {
        reviewRepository.deleteById(id);
        logger.info("Review {} deleted", id);
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return reviewRepository.findById(id);

    }
    @Override
    public List<Review> findAllByProperty(Property property) {
        return reviewRepository.findAllByProperty(property);
    }

    @Override
    public List<Review> findAllByAuthor(User author) {
        return reviewRepository.findAllByAuthor(author);
    }

    @Override
    public boolean hasUserReviewedProperty(User author, Property property) {
        return reviewRepository.existsByAuthorAndProperty(author, property);
    }
}