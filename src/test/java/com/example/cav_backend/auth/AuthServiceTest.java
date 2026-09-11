package com.example.cav_backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.cav_backend.user.AppUser;
import com.example.cav_backend.user.AppUserRepository;
import com.example.cav_backend.user.Role;

class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        authService = new AuthService(
                appUserRepository,
                passwordEncoder,
                authenticationManager,
                jwtService);
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        when(
                appUserRepository.existsByUsername("newuser")).thenReturn(false);

        when(
                passwordEncoder.encode("password123")).thenReturn("encoded-password");

        authService.register(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository)
                .save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertEquals(
                "newuser",
                savedUser.getUsername());

        assertEquals(
                "encoded-password",
                savedUser.getPassword());

        assertEquals(
                Role.USER,
                savedUser.getRole());

        verify(passwordEncoder)
                .encode("password123");
    }

    @Test
    void shouldRejectDuplicateUsername() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        when(
                appUserRepository.existsByUsername(
                        "existinguser"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request));

        assertEquals(
                "Username already exists: existinguser",
                exception.getMessage());

        verify(passwordEncoder, never())
                .encode(any());

        verify(appUserRepository, never())
                .save(any());
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = new LoginRequest();
        request.setUsername("giray");
        request.setPassword("password123");

        AppUser user = new AppUser(
                "giray",
                "encoded-password");

        when(
                appUserRepository.findByUsername("giray")).thenReturn(Optional.of(user));

        when(
                jwtService.generateToken(
                        "giray",
                        Role.USER))
                .thenReturn("test-jwt-token");

        String token = authService.login(request);

        assertEquals(
                "test-jwt-token",
                token);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationCaptor = ArgumentCaptor.forClass(
                UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager)
                .authenticate(
                        authenticationCaptor.capture());

        UsernamePasswordAuthenticationToken authentication = authenticationCaptor.getValue();

        assertEquals(
                "giray",
                authentication.getPrincipal());

        assertEquals(
                "password123",
                authentication.getCredentials());

        verify(jwtService)
                .generateToken(
                        "giray",
                        Role.USER);
    }

    @Test
    void shouldRejectLoginWhenUserIsNotFound() {

        LoginRequest request = new LoginRequest();
        request.setUsername("missinguser");
        request.setPassword("password123");

        when(
                appUserRepository.findByUsername(
                        "missinguser"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request));

        assertEquals(
                "User not found: missinguser",
                exception.getMessage());

        verify(authenticationManager)
                .authenticate(any());

        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectInvalidCredentials() {

        LoginRequest request = new LoginRequest();
        request.setUsername("giray");
        request.setPassword("wrong-password");

        when(
                authenticationManager.authenticate(any())).thenThrow(
                        new BadCredentialsException(
                                "Bad credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request));

        verify(
                appUserRepository,
                never()).findByUsername(any());

        verifyNoInteractions(jwtService);
    }
}