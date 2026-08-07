package com.softuni.stayeasy.service.reservation;

import com.softuni.stayeasy.exception.ReservationNotFoundException;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.reservation.Reservation;
import com.softuni.stayeasy.model.entity.reservation.ReservationStatus;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.repository.reservation.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private UUID reservationId;
    private Reservation reservation;
    private User renter;
    private Property property;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        renter = User.builder().id(UUID.randomUUID()).build();
        property = Property.builder().id(UUID.randomUUID()).build();
        reservation = Reservation.builder()
                .id(reservationId)
                .status(ReservationStatus.PENDING)
                .renter(renter)
                .property(property)
                .checkIn(LocalDate.now().plusDays(1))
                .checkOut(LocalDate.now().plusDays(3))
                .build();
    }

    @Test
    void createReservation_setsStatusPendingAndSaves() {
        reservationService.createReservation(reservation);

        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertNotNull(reservation.getCreated());
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void cancelReservation_existingReservation_setsStatusCancelled() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(reservationId);

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void cancelReservation_nonExistentReservation_throwsException() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> reservationService.cancelReservation(reservationId));
    }

    @Test
    void approveReservation_existingReservation_setsStatusApproved() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.approveReservation(reservationId);

        assertEquals(ReservationStatus.APPROVED, reservation.getStatus());
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void rejectReservation_existingReservation_setsStatusRejected() {
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        reservationService.rejectReservation(reservationId);

        assertEquals(ReservationStatus.REJECTED, reservation.getStatus());
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void findAllByRenter_returnsRenterReservations() {
        when(reservationRepository.findAllByRenter(renter)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.findAllByRenter(renter);

        assertEquals(1, result.size());
    }

    @Test
    void findAllByProperty_returnsPropertyReservations() {
        when(reservationRepository.findAllByProperty(property)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.findAllByProperty(property);

        assertEquals(1, result.size());
    }

    @Test
    void expireStalePendingReservations_marksOldPendingAsExpired() {
        Reservation staleReservation = Reservation.builder()
                .id(UUID.randomUUID())
                .status(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findAllByStatusAndCreatedBefore(eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(staleReservation));

        int count = reservationService.expireStalePendingReservations();

        assertEquals(1, count);
        assertEquals(ReservationStatus.EXPIRED, staleReservation.getStatus());
        verify(reservationRepository, times(1)).saveAll(List.of(staleReservation));
    }

    @Test
    void expireStalePendingReservations_noStaleReservations_returnsZero() {
        when(reservationRepository.findAllByStatusAndCreatedBefore(eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        int count = reservationService.expireStalePendingReservations();

        assertEquals(0, count);
    }

    @Test
    void completePastReservations_marksPastApprovedAsCompleted() {
        Reservation pastReservation = Reservation.builder()
                .id(UUID.randomUUID())
                .status(ReservationStatus.APPROVED)
                .build();

        when(reservationRepository.findAllByStatusAndCheckOutBefore(eq(ReservationStatus.APPROVED), any(LocalDate.class)))
                .thenReturn(List.of(pastReservation));

        int count = reservationService.completePastReservations();

        assertEquals(1, count);
        assertEquals(ReservationStatus.COMPLETED, pastReservation.getStatus());
        verify(reservationRepository, times(1)).saveAll(List.of(pastReservation));
    }
}