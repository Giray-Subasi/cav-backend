package com.example.cav_backend.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.cav_backend.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        AuthController authController = new AuthController(authService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(
                        new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldRegisterUserAndReturn201() throws Exception {

        String requestBody = """
                {
                  "username": "giray",
                  "password": "password123"
                }
                """;

        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        verify(authService)
                .register(any(RegisterRequest.class));
    }

    @Test
    void shouldReturn400WhenRegisterUsernameIsBlank()
            throws Exception {

        String requestBody = """
                {
                  "username": "",
                  "password": "password123"
                }
                """;

        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.error")
                                .value("VALIDATION_ERROR"));

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturn400WhenRegisterPasswordIsTooShort()
            throws Exception {

        String requestBody = """
                {
                  "username": "giray",
                  "password": "123"
                }
                """;

        mockMvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.error")
                                .value("VALIDATION_ERROR"));

        verifyNoInteractions(authService);
    }

    @Test
    void shouldLoginAndReturnToken() throws Exception {

        when(
                authService.login(
                        any(LoginRequest.class)))
                .thenReturn("mock-jwt-token");

        String requestBody = """
                {
                  "username": "giray",
                  "password": "password123"
                }
                """;

        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .value("mock-jwt-token"));

        verify(authService)
                .login(any(LoginRequest.class));
    }

    @Test
    void shouldReturn400WhenLoginUsernameIsBlank()
            throws Exception {

        String requestBody = """
                {
                  "username": "",
                  "password": "password123"
                }
                """;

        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.error")
                                .value("VALIDATION_ERROR"));

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturn400WhenLoginPasswordIsBlank()
            throws Exception {

        String requestBody = """
                {
                  "username": "giray",
                  "password": ""
                }
                """;

        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.error")
                                .value("VALIDATION_ERROR"));

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturn401WhenLoginCredentialsAreInvalid()
            throws Exception {

        when(
                authService.login(
                        any(LoginRequest.class)))
                .thenThrow(
                        new BadCredentialsException(
                                "Bad credentials"));

        String requestBody = """
                {
                  "username": "admin",
                  "password": "wrong-password"
                }
                """;

        mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.status").value(401))
                .andExpect(
                        jsonPath("$.error")
                                .value("UNAUTHORIZED"))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid username or password"));
    }
}