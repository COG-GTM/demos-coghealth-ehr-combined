package com.medchart.ehr.controller;

import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.controller.AuthController.LoginRequest;
import com.medchart.ehr.controller.AuthController.SignUpRequest;
import com.medchart.ehr.controller.AuthController.UserResponse;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    private AuthController authController;
    private User user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authenticationManager, userRepository, passwordEncoder, tokenProvider);
        user = User.builder()
            .id(1L)
            .username("testuser")
            .email("testuser@example.com")
            .firstName("Test")
            .lastName("User")
            .password("encoded-password")
            .roles(Set.of(User.Role.PROVIDER))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();
        authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAuthenticateUserSuccess() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token-12345");
        when(tokenProvider.getJwtExpirationInMs()).thenReturn(86400000L);

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("jwt-token-12345", body.get("token"));
        assertEquals("Bearer", body.get("type"));
        assertEquals(86400000L, body.get("expiresIn"));
        UserResponse responseUser = assertInstanceOf(UserResponse.class, body.get("user"));
        assertEquals("testuser", responseUser.getUsername());
    }

    @Test
    void testAuthenticateUserFailure() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("Invalid username or password", body.get("message"));
    }

    @Test
    void testRegisterUserSuccess() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setFirstName("New");
        signUpRequest.setLastName("User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        UserResponse body = assertInstanceOf(UserResponse.class, response.getBody());
        assertEquals("newuser", body.getUsername());
        assertEquals("newuser@example.com", body.getEmail());
        assertEquals(Set.of("PROVIDER"), Set.copyOf(body.getRoles()));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encoded-password", userCaptor.getValue().getPassword());
    }

    @Test
    void testRegisterUserUsernameTaken() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("existinguser");
        signUpRequest.setEmail("newuser@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setFirstName("New");
        signUpRequest.setLastName("User");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is already taken!", response.getBody());
    }

    @Test
    void testRegisterUserEmailTaken() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("existing@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setFirstName("New");
        signUpRequest.setLastName("User");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already in use!", response.getBody());
    }

    @Test
    void testGetCurrentUser() {
        ResponseEntity<?> response = authController.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponse body = assertInstanceOf(UserResponse.class, response.getBody());
        assertEquals("testuser", body.getUsername());
    }

    @Test
    void testValidateToken() {
        ResponseEntity<?> response = authController.validateToken(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertTrue((Boolean) body.get("valid"));
        assertInstanceOf(UserResponse.class, body.get("user"));
    }

    @Test
    void testValidateTokenWithoutAuthentication() {
        ResponseEntity<?> response = authController.validateToken(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertFalse((Boolean) body.get("valid"));
    }

    @Test
    void testRefreshToken() {
        when(tokenProvider.generateToken(authentication)).thenReturn("refreshed-token");
        when(tokenProvider.getJwtExpirationInMs()).thenReturn(86400000L);

        ResponseEntity<?> response = authController.refreshToken(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("refreshed-token", body.get("token"));
        assertEquals("Bearer", body.get("type"));
        assertInstanceOf(UserResponse.class, body.get("user"));
    }
}
