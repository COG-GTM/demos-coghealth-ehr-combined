package com.medchart.ehr.controller;

import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private AuthController authController;

    private AuthController.LoginRequest loginRequest;
    private AuthController.SignUpRequest signUpRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("drsmith");
        loginRequest.setPassword("password123");

        signUpRequest = new AuthController.SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("newuser@medchart.com");
        signUpRequest.setPassword("securePass1");
        signUpRequest.setFirstName("New");
        signUpRequest.setLastName("User");
    }

    // --- Login Tests ---

    @Test
    void login_withValidCredentials_returnsTokenResponse() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("jwt-token-value");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token-value", body.get("token"));
        assertEquals("Bearer", body.get("type"));
    }

    @Test
    void login_withInvalidCredentials_throwsBadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authController.authenticateUser(loginRequest));
    }

    @Test
    void login_passesCorrectCredentialsToAuthManager() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("token");

        authController.authenticateUser(loginRequest);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals("drsmith", captor.getValue().getPrincipal());
        assertEquals("password123", captor.getValue().getCredentials());
    }

    // --- Registration Tests ---

    @Test
    void register_withNewUser_returnsSuccess() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("securePass1")).thenReturn("encoded_pass");

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void register_savesUserWithEncodedPassword() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("securePass1")).thenReturn("encoded_pass");

        authController.registerUser(signUpRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("newuser@medchart.com", savedUser.getEmail());
        assertEquals("encoded_pass", savedUser.getPassword());
        assertEquals("New", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertTrue(savedUser.getEnabled());
    }

    @Test
    void register_assignsDefaultProviderRole() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("securePass1")).thenReturn("encoded_pass");

        authController.registerUser(signUpRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().getRoles().contains(User.Role.PROVIDER));
        assertEquals(1, captor.getValue().getRoles().size());
    }

    @Test
    void register_withExistingUsername_returnsBadRequest() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is already taken!", response.getBody());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withExistingEmail_returnsBadRequest() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@medchart.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already in use!", response.getBody());
        verify(userRepository, never()).save(any());
    }

    // --- DTO Tests ---

    @Test
    void loginRequest_gettersAndSetters() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("user1");
        req.setPassword("pass1");

        assertEquals("user1", req.getUsername());
        assertEquals("pass1", req.getPassword());
    }

    @Test
    void signUpRequest_gettersAndSetters() {
        AuthController.SignUpRequest req = new AuthController.SignUpRequest();
        req.setUsername("user1");
        req.setEmail("user1@test.com");
        req.setPassword("pass1");
        req.setFirstName("First");
        req.setLastName("Last");

        assertEquals("user1", req.getUsername());
        assertEquals("user1@test.com", req.getEmail());
        assertEquals("pass1", req.getPassword());
        assertEquals("First", req.getFirstName());
        assertEquals("Last", req.getLastName());
    }
}
