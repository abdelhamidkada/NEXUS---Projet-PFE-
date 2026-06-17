package com.dyxia.nexuserp.service;

import com.dyxia.nexuserp.exception.ResourceNotFoundException;
import com.dyxia.nexuserp.exception.TokenRefreshException;
import com.dyxia.nexuserp.model.RefreshToken;
import com.dyxia.nexuserp.model.User;
import com.dyxia.nexuserp.repository.RefreshTokenRepository;
import com.dyxia.nexuserp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class RefreshTokenServiceTest {

    private RefreshTokenRepository refreshTokenRepository;
    private UserRepository userRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository);
    }

    @Test
    void testCreateRefreshTokenSuccess() {
        User user = User.builder()
                .id(1L)
                .email("test@dyxia.fr")
                .firstName("John")
                .lastName("Doe")
                .build();

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Mockito.when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken createdToken = refreshTokenService.createRefreshToken(1L);

        assertNotNull(createdToken);
        assertNotNull(createdToken.getToken());
        assertEquals(user, createdToken.getUser());
        
        // Assert expiry date is approx 7 days in the future (within a 5-second range to avoid timing skew)
        Instant expectedExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
        long diffSeconds = Math.abs(createdToken.getExpiryDate().getEpochSecond() - expectedExpiry.getEpochSecond());
        assertTrue(diffSeconds < 5, "Expiry date should be 7 days in the future");

        Mockito.verify(refreshTokenRepository, Mockito.times(1)).deleteByUserId(1L);
        Mockito.verify(refreshTokenRepository, Mockito.times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testCreateRefreshTokenUserNotFound() {
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                refreshTokenService.createRefreshToken(99L)
        );

        Mockito.verify(refreshTokenRepository, Mockito.never()).deleteByUserId(any());
        Mockito.verify(refreshTokenRepository, Mockito.never()).save(any());
    }

    @Test
    void testVerifyExpirationValidToken() {
        RefreshToken token = RefreshToken.builder()
                .token("valid-uuid-token")
                .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        RefreshToken verified = refreshTokenService.verifyExpiration(token);

        assertEquals(token, verified);
        Mockito.verify(refreshTokenRepository, Mockito.never()).delete(any());
    }

    @Test
    void testVerifyExpirationExpiredToken() {
        RefreshToken token = RefreshToken.builder()
                .token("expired-uuid-token")
                .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        assertThrows(TokenRefreshException.class, () ->
                refreshTokenService.verifyExpiration(token)
        );

        Mockito.verify(refreshTokenRepository, Mockito.times(1)).delete(token);
    }

    @Test
    void testDeleteByUserId() {
        refreshTokenService.deleteByUserId(1L);
        Mockito.verify(refreshTokenRepository, Mockito.times(1)).deleteByUserId(1L);
    }
}
