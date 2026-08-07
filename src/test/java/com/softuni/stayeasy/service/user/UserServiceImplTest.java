package com.softuni.stayeasy.service.user;

import com.softuni.stayeasy.model.entity.user.User;
import com.softuni.stayeasy.model.entity.user.UserRole;
import com.softuni.stayeasy.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .username("renter1")
                .email("renter1@stayeasy.com")
                .role(UserRole.RENTER)
                .build();
    }

    @Test
    void register_encodesPasswordAndSavesWithRenterRole() {
        when(passwordEncoder.encode("Renter123!")).thenReturn("hashed-password");

        userService.register("renter1", "renter1@stayeasy.com", "Renter123!", "Renter", "One");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("renter1", savedUser.getUsername());
        assertEquals("hashed-password", savedUser.getPassword());
        assertEquals(UserRole.RENTER, savedUser.getRole());
    }

    @Test
    void findByUsername_existingUser_returnsUser() {
        when(userRepository.findByUsername("renter1")).thenReturn(Optional.of(user));

        Optional<User> found = userService.findByUsername("renter1");

        assertTrue(found.isPresent());
        assertEquals("renter1", found.get().getUsername());
    }

    @Test
    void findById_existingUser_returnsUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> found = userService.findById(userId);

        assertTrue(found.isPresent());
    }

    @Test
    void existsByUsername_returnsRepositoryResult() {
        when(userRepository.existsByUsername("renter1")).thenReturn(true);

        assertTrue(userService.existsByUsername("renter1"));
    }

    @Test
    void existsByEmail_returnsRepositoryResult() {
        when(userRepository.existsByEmail("renter1@stayeasy.com")).thenReturn(true);

        assertTrue(userService.existsByEmail("renter1@stayeasy.com"));
    }

    @Test
    void updateUser_updatesTimestampAndSaves() {
        userService.updateUser(user);

        assertNotNull(user.getUpdatedOn());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void changeRole_existingUser_updatesRoleAndSaves() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeRole(userId, UserRole.HOST);

        assertEquals(UserRole.HOST, user.getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void changeRole_nonExistentUser_doesNothing() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        userService.changeRole(userId, UserRole.HOST);

        verify(userRepository, never()).save(any());
    }
}