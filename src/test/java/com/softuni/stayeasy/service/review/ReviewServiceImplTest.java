package com.softuni.stayeasy.service.review;

import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.review.Review;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.repository.review.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User author;
    private Property property;
    private Review review;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        author = User.builder().id(UUID.randomUUID()).build();
        property = Property.builder().id(UUID.randomUUID()).build();
        review = Review.builder()
                .id(reviewId)
                .content("Great stay!")
                .rating(5)
                .author(author)
                .property(property)
                .build();
    }

    @Test
    void createReview_setsCreatedOnAndSaves() {
        reviewService.createReview(review);

        assertNotNull(review.getCreatedOn());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void deleteReview_callsRepositoryDeleteById() {
        reviewService.deleteReview(reviewId);

        verify(reviewRepository, times(1)).deleteById(reviewId);
    }

    @Test
    void findById_existingReview_returnsReview() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        Optional<Review> found = reviewService.findById(reviewId);

        assertTrue(found.isPresent());
        assertEquals("Great stay!", found.get().getContent());
    }

    @Test
    void findById_nonExistentReview_returnsEmpty() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        Optional<Review> found = reviewService.findById(reviewId);

        assertTrue(found.isEmpty());
    }

    @Test
    void findAllByProperty_returnsPropertyReviews() {
        when(reviewRepository.findAllByProperty(property)).thenReturn(List.of(review));

        List<Review> result = reviewService.findAllByProperty(property);

        assertEquals(1, result.size());
    }

    @Test
    void findAllByAuthor_returnsAuthorReviews() {
        when(reviewRepository.findAllByAuthor(author)).thenReturn(List.of(review));

        List<Review> result = reviewService.findAllByAuthor(author);

        assertEquals(1, result.size());
    }

    @Test
    void hasUserReviewedProperty_userHasReviewed_returnsTrue() {
        when(reviewRepository.existsByAuthorAndProperty(author, property)).thenReturn(true);

        boolean result = reviewService.hasUserReviewedProperty(author, property);

        assertTrue(result);
    }

    @Test
    void hasUserReviewedProperty_userHasNotReviewed_returnsFalse() {
        when(reviewRepository.existsByAuthorAndProperty(author, property)).thenReturn(false);

        boolean result = reviewService.hasUserReviewedProperty(author, property);

        assertFalse(result);
    }
}