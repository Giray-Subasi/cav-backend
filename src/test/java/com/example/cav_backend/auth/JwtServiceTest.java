package com.example.cav_backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.example.cav_backend.user.Role;

class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        jwtService = new JwtService(jwtEncoder);
    }

    @Test
    void shouldGenerateTokenSuccessfully() {

        Jwt encodedJwt = mock(Jwt.class);

        when(encodedJwt.getTokenValue())
                .thenReturn("test-jwt-token");

        when(jwtEncoder.encode(any()))
                .thenReturn(encodedJwt);

        String token = jwtService.generateToken(
                "giray",
                Role.USER);

        assertEquals(
                "test-jwt-token",
                token);

        ArgumentCaptor<JwtEncoderParameters> parametersCaptor = ArgumentCaptor.forClass(
                JwtEncoderParameters.class);

        verify(jwtEncoder)
                .encode(parametersCaptor.capture());

        JwtEncoderParameters parameters = parametersCaptor.getValue();

        JwtClaimsSet claims = parameters.getClaims();

        assertEquals(
                "cav-backend",
                claims.getClaim("iss"));

        assertEquals(
                "giray",
                claims.getSubject());

        assertEquals(
                "USER",
                claims.getClaim("role"));

        assertNotNull(
                claims.getIssuedAt());

        assertNotNull(
                claims.getExpiresAt());

        assertEquals(
                3600,
                Duration.between(
                        claims.getIssuedAt(),
                        claims.getExpiresAt()).getSeconds());
    }

    @Test
    void shouldIncludeAdminRoleInToken() {

        Jwt encodedJwt = mock(Jwt.class);

        when(encodedJwt.getTokenValue())
                .thenReturn("admin-test-token");

        when(jwtEncoder.encode(any()))
                .thenReturn(encodedJwt);

        jwtService.generateToken(
                "admin",
                Role.ADMIN);

        ArgumentCaptor<JwtEncoderParameters> parametersCaptor = ArgumentCaptor.forClass(
                JwtEncoderParameters.class);

        verify(jwtEncoder)
                .encode(parametersCaptor.capture());

        JwtClaimsSet claims = parametersCaptor
                .getValue()
                .getClaims();

        assertEquals(
                "admin",
                claims.getSubject());

        assertEquals(
                "ADMIN",
                claims.getClaim("role"));
    }
}