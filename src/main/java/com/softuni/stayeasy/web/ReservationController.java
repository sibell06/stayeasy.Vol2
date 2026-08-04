package com.softuni.stayeasy.web;

import com.softuni.stayeasy.client.LoyaltyServiceClient;
import com.softuni.stayeasy.model.dto.reservation.ReservationBindingModel;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.reservation.Reservation;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.security.UserPrincipal;
import com.softuni.stayeasy.service.property.PropertyService;
import com.softuni.stayeasy.service.reservation.ReservationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final PropertyService propertyService;
    private final LoyaltyServiceClient loyaltyServiceClient;

    public ReservationController(ReservationService reservationService,
                                 PropertyService propertyService,
                                 LoyaltyServiceClient loyaltyServiceClient) {
        this.reservationService = reservationService;
        this.propertyService = propertyService;
        this.loyaltyServiceClient = loyaltyServiceClient;
    }

    @GetMapping("/create/{propertyId}")
    public String createPage(@PathVariable UUID propertyId, Model model) {
        Optional<Property> propertyOpt = propertyService.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return "redirect:/properties";
        }
        model.addAttribute("property", propertyOpt.get());
        model.addAttribute("reservationData", new ReservationBindingModel());
        return "reservation/create";
    }

    @PostMapping("/create/{propertyId}")
    public String create(@PathVariable UUID propertyId,
                         @Valid @ModelAttribute("reservationData") ReservationBindingModel reservationData,
                         BindingResult bindingResult,
                         Model model,
                         @AuthenticationPrincipal UserPrincipal principal) {

        Optional<Property> propertyOpt = propertyService.findById(propertyId);
        if (propertyOpt.isEmpty()) {
            return "redirect:/properties";
        }

        Property property = propertyOpt.get();

        if (bindingResult.hasErrors()) {
            model.addAttribute("property", property);
            return "reservation/create";
        }

        if (reservationData.getCheckIn().isBefore(LocalDate.now())) {
            model.addAttribute("property", property);
            model.addAttribute("pastDateError", true);
            return "reservation/create";
        }

        if (!reservationData.getCheckOut().isAfter(reservationData.getCheckIn())) {
            model.addAttribute("property", property);
            model.addAttribute("dateError", true);
            return "reservation/create";
        }

        if (reservationData.getGuests() > property.getMaxGuest()) {
            model.addAttribute("property", property);
            model.addAttribute("guestError", true);
            return "reservation/create";
        }

        long nights = ChronoUnit.DAYS.between(reservationData.getCheckIn(), reservationData.getCheckOut());
        BigDecimal totalPrice = property.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation reservation = Reservation.builder()
                .checkIn(reservationData.getCheckIn())
                .checkOut(reservationData.getCheckOut())
                .guests(reservationData.getGuests())
                .totalPrice(totalPrice)
                .renter(principal.getUser())
                .property(property)
                .build();

        reservationService.createReservation(reservation);
        return "redirect:/reservations/my";
    }

    @GetMapping("/my")
    public String myReservation(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        model.addAttribute("reservations", reservationService.findAllByRenter(principal.getUser()));
        return "reservation/my-reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        Optional<Reservation> reservationOpt = reservationService.findById(id);
        if (reservationOpt.isEmpty()) {
            return "redirect:/reservations/my";
        }

        boolean isOwner = reservationOpt.get().getRenter().getId().equals(principal.getId());
        boolean isAdmin = principal.getUser().getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/reservations/my";
        }
        reservationService.cancelReservation(id);
        return "redirect:/reservations/my";
    }

    @GetMapping("/host")
    public String hostDashboard(Model model, @AuthenticationPrincipal UserPrincipal principal) {
        var hostProperties = propertyService.findAllByHost(principal.getUser());
        var allReservations = hostProperties.stream()
                .flatMap(p -> reservationService.findAllByProperty(p).stream())
                .toList();
        model.addAttribute("reservations", allReservations);
        model.addAttribute("properties", hostProperties);
        return "reservation/host-dashboard";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        Optional<Reservation> reservationOpt = reservationService.findById(id);
        if (reservationOpt.isEmpty()) {
            return "redirect:/reservations/host";
        }

        Reservation reservation = reservationOpt.get();
        boolean isOwner = reservation.getProperty().getHost().getId().equals(principal.getId());
        boolean isAdmin = principal.getUser().getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/reservations/host";
        }

        reservationService.approveReservation(id);

        try {
            long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
            loyaltyServiceClient.awardPoints(reservation.getRenter().getId(), (int) nights);
        } catch (Exception ex) {
            System.err.println("Failed to award loyalty points: " + ex.getMessage());
        }

        return "redirect:/reservations/host";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        Optional<Reservation> reservationOpt = reservationService.findById(id);
        if (reservationOpt.isEmpty()) {
            return "redirect:/reservations/host";
        }

        boolean isOwner = reservationOpt.get().getProperty().getHost().getId().equals(principal.getId());
        boolean isAdmin = principal.getUser().getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/reservations/host";
        }
        reservationService.rejectReservation(id);
        return "redirect:/reservations/host";
    }
}