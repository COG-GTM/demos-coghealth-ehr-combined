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
import java.util.Set;

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
        loginRequest.setUsername("dr.smith");
        loginRequest.setPassword("securePass123");

        signUpRequest = new AuthController.SignUpRequest();
        signUpRequest.setUsername("new.doctor");
        signUpRequest.setEmail("new.doctor@medchart.com");
        signUpRequest.setPassword("newPass456");
        signUpRequest.setFirstName("New");
        signUpRequest.setLastName("Doctor");
    }

    // --- Login tests ---

    @Test
    void authenticateUser_returnsTokenOnSuccess() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("jwt-token-123");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token-123", body.get("token"));
        assertEquals("Bearer", body.get("type"));
    }

    @Test
    void authenticateUser_authenticatesWithCorrectCredentials() {
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("token");

        authController.authenticateUser(loginRequest);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());

        assertEquals("dr.smith", captor.getValue().getPrincipal());
        assertEquals("securePass123", captor.getValue().getCredentials());
    }

    @Test
    void authenticateUser_propagatesBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authController.authenticateUser(loginRequest));
    }

    // --- Registration tests ---

    @Test
    void registerUser_returnsOkOnSuccess() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("newPass456")).thenReturn("encoded-password");

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void registerUser_savesUserWithEncodedPassword() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("newPass456")).thenReturn("encoded-password");

        authController.registerUser(signUpRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("new.doctor", savedUser.getUsername());
        assertEquals("new.doctor@medchart.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("New", savedUser.getFirstName());
        assertEquals("Doctor", savedUser.getLastName());
    }

    @Test
    void registerUser_assignsDefaultProviderRole() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        authController.registerUser(signUpRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        Set<User.Role> roles = userCaptor.getValue().getRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains(User.Role.PROVIDER));
    }

    @Test
    void registerUser_enablesAccountByDefault() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        authController.registerUser(signUpRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.isEnabled());
        assertTrue(savedUser.isAccountNonExpired());
        assertTrue(savedUser.isAccountNonLocked());
        assertTrue(savedUser.isCredentialsNonExpired());
    }

    @Test
    void registerUser_returnsBadRequestForDuplicateUsername() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is already taken!", response.getBody());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_returnsBadRequestForDuplicateEmail() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already in use!", response.getBody());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_checksUsernameBeforeEmail() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(true);

        authController.registerUser(signUpRequest);

        verify(userRepository, never()).existsByEmail(any());
    }
}
