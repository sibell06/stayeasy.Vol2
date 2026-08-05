package com.softuni.stayeasy.scheduling;

import com.softuni.stayeasy.service.reservation.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReservationScheduler.class);

    private final ReservationService reservationService;

    public ReservationScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void expireStalePendingReservations() {
        int expiredCount = reservationService.expireStalePendingReservations();
        logger.info("Scheduled job: expired {} stale pending reservations.", expiredCount);
    }

    @Scheduled(fixedRate = 60000)
    public void completePastReservations() {
        int completedCount = reservationService.completePastReservations();
        logger.info("Scheduled job: marked {} reservations as completed.", completedCount);
    }
}