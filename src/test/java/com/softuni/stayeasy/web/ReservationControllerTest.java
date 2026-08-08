package com.softuni.stayeasy.web;

import com.softuni.stayeasy.client.LoyaltyServiceClient;
import com.softuni.stayeasy.config.SecurityConfiguration;
import com.softuni.stayeasy.model.entity.property.Property;
import com.softuni.stayeasy.model.entity.reservation.Reservation;
import com.softuni.stayeasy.model.entity.reservation.ReservationStatus;
import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.security.CustomUserDetailsService;
import com.softuni.stayeasy.security.UserPrincipal;
import com.softuni.stayeasy.service.property.PropertyService;
import com.softuni.stayeasy.service.reservation.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import(SecurityConfiguration.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private PropertyService propertyService;

    @MockitoBean
    private LoyaltyServiceClient loyaltyServiceClient;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private User renter;
    private User host;
    private Property property;
    private UserPrincipal renterPrincipal;
    private UserPrincipal hostPrincipal;

    private void setUpUsers() {
        renter = User.builder().id(UUID.randomUUID()).username("renter1").password("x").role(UserRole.RENTER).build();
        host = User.builder().id(UUID.randomUUID()).username("host1").password("x").role(UserRole.HOST).build();
        property = Property.builder().id(UUID.randomUUID()).title("Test").host(host).maxGuest(4)
                .pricePerNight(new BigDecimal("50.00")).build();
        renterPrincipal = new UserPrincipal(renter);
        hostPrincipal = new UserPrincipal(host);
    }

    @Test
    void createPage_validProperty_returnsOkWithBalance() throws Exception {
        setUpUsers();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));
        when(loyaltyServiceClient.getBalance(renter.getId())).thenReturn(Map.of("pointsBalance", 50));

        mockMvc.perform(get("/reservations/create/{propertyId}", property.getId()).with(user(renterPrincipal)))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/create"));
    }

    @Test
    void create_validReservation_redirectsToMyReservations() throws Exception {
        setUpUsers();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(post("/reservations/create/{propertyId}", property.getId())
                        .with(user(renterPrincipal))
                        .with(csrf())
                        .param("checkIn", LocalDate.now().plusDays(1).toString())
                        .param("checkOut", LocalDate.now().plusDays(3).toString())
                        .param("guests", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/my"));
    }

    @Test
    void create_pastCheckInDate_returnsCreateViewWithValidationError() throws Exception {
        setUpUsers();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(post("/reservations/create/{propertyId}", property.getId())
                        .with(user(renterPrincipal))
                        .with(csrf())
                        .param("checkIn", LocalDate.now().minusDays(1).toString())
                        .param("checkOut", LocalDate.now().plusDays(3).toString())
                        .param("guests", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/create"))
                .andExpect(model().attributeHasFieldErrors("reservationData", "checkIn"));
    }

    @Test
    void create_tooManyGuests_returnsCreateViewWithError() throws Exception {
        setUpUsers();
        when(propertyService.findById(property.getId())).thenReturn(Optional.of(property));

        mockMvc.perform(post("/reservations/create/{propertyId}", property.getId())
                        .with(user(renterPrincipal))
                        .with(csrf())
                        .param("checkIn", LocalDate.now().plusDays(1).toString())
                        .param("checkOut", LocalDate.now().plusDays(3).toString())
                        .param("guests", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("guestError", true));
    }

    @Test
    void myReservations_returnsOkWithRenterReservations() throws Exception {
        setUpUsers();
        when(reservationService.findAllByRenter(renter)).thenReturn(List.of());

        mockMvc.perform(get("/reservations/my").with(user(renterPrincipal)))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/my-reservations"));
    }

    @Test
    void cancel_ownerRenter_redirectsToMyReservations() throws Exception {
        setUpUsers();
        Reservation reservation = Reservation.builder().id(UUID.randomUUID()).status(ReservationStatus.PENDING)
                .renter(renter).property(property).build();
        when(reservationService.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        mockMvc.perform(post("/reservations/{id}/cancel", reservation.getId())
                        .with(user(renterPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/my"));
    }

    @Test
    void hostDashboard_returnsOkWithHostReservations() throws Exception {
        setUpUsers();
        when(propertyService.findAllByHost(host)).thenReturn(List.of(property));
        when(reservationService.findAllByProperty(property)).thenReturn(List.of());

        mockMvc.perform(get("/reservations/host").with(user(hostPrincipal)))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/host-dashboard"));
    }

    @Test
    void approve_ownerHost_redirectsToHostDashboard() throws Exception {
        setUpUsers();
        Reservation reservation = Reservation.builder().id(UUID.randomUUID()).status(ReservationStatus.PENDING)
                .renter(renter).property(property)
                .checkIn(LocalDate.now()).checkOut(LocalDate.now().plusDays(2)).build();
        when(reservationService.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(loyaltyServiceClient.awardPoints(any())).thenReturn(Map.of("pointsBalance", 20));

        mockMvc.perform(post("/reservations/{id}/approve", reservation.getId())
                        .with(user(hostPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/host"));
    }

    @Test
    void reject_ownerHost_redirectsToHostDashboard() throws Exception {
        setUpUsers();
        Reservation reservation = Reservation.builder().id(UUID.randomUUID()).status(ReservationStatus.PENDING)
                .renter(renter).property(property).build();
        when(reservationService.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        mockMvc.perform(post("/reservations/{id}/reject", reservation.getId())
                        .with(user(hostPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/host"));
    }

    @Test
    void cancel_nonOwnerRenter_redirectsWithoutCancelling() throws Exception {
        setUpUsers();
        User otherRenter = User.builder().id(UUID.randomUUID()).username("other").password("x").role(UserRole.RENTER).build();
        Reservation reservation = Reservation.builder().id(UUID.randomUUID()).status(ReservationStatus.PENDING)
                .renter(otherRenter).property(property).build();
        when(reservationService.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        mockMvc.perform(post("/reservations/{id}/cancel", reservation.getId())
                        .with(user(renterPrincipal))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/my"));
    }
}