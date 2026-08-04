package com.softuni.stayeasy.scheduling;

import com.softuni.stayeasy.service.reservation.ReservationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationScheduler {

    private final ReservationService reservationService;

    public ReservationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void expireStalePendingReservations() {
        int expiredCount = reservationService.expireStalePendingReservations();
        System.out.println("Scheduled job: expired " + expiredCount + " stale pending reservations.");
    }

    @Scheduled(fixedRate = 60000)
    public void completePastReservations() {
        int completedCount = reservationService.completePastReservations();
        System.out.println("Scheduled job: marked " + completedCount + " reservations as completed.");
    }
}