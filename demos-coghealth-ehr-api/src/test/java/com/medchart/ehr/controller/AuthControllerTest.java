package com.medchart.ehr.controller;

import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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
        loginRequest.setUsername("dr.smith");
        loginRequest.setPassword("password123");

        signUpRequest = new AuthController.SignUpRequest();
        signUpRequest.setUsername("new.doctor");
        signUpRequest.setEmail("new.doctor@medchart.com");
        signUpRequest.setPassword("securepass");
        signUpRequest.setFirstName("New");
        signUpRequest.setLastName("Doctor");
    }

    @Test
    void login_withValidCredentials_returnsTokenResponse() {
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User("dr.smith", "password123",
                        List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("mock-jwt-token");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(200, response.getStatusCodeValue());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("mock-jwt-token", body.get("token"));
        assertEquals("Bearer", body.get("type"));
    }

    @Test
    void login_withValidCredentials_authenticatesWithCorrectCredentials() {
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User("dr.smith", "password123",
                        List.of(new SimpleGrantedAuthority("ROLE_PROVIDER")));
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("token");

        authController.authenticateUser(loginRequest);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(auth);
    }

    @Test
    void login_withInvalidCredentials_throwsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authController.authenticateUser(loginRequest));
    }

    @Test
    void register_withNewUser_returnsSuccess() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("securepass")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withNewUser_encodesPassword() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("securepass")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        authController.registerUser(signUpRequest);

        verify(passwordEncoder).encode("securepass");
    }

    @Test
    void register_withExistingUsername_returnsBadRequest() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Username is already taken!", response.getBody());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withExistingEmail_returnsBadRequest() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email is already in use!", response.getBody());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_assignsDefaultProviderRole() {
        when(userRepository.existsByUsername("new.doctor")).thenReturn(false);
        when(userRepository.existsByEmail("new.doctor@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(userRepository).save(argThat(user ->
                user.getRoles().contains(User.Role.PROVIDER) && user.getRoles().size() == 1
        ));
    }

    @Test
    void loginRequest_gettersAndSetters() {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername("user");
        req.setPassword("pass");

        assertEquals("user", req.getUsername());
        assertEquals("pass", req.getPassword());
    }

    @Test
    void signUpRequest_gettersAndSetters() {
        AuthController.SignUpRequest req = new AuthController.SignUpRequest();
        req.setUsername("user");
        req.setEmail("user@test.com");
        req.setPassword("pass");
        req.setFirstName("First");
        req.setLastName("Last");

        assertEquals("user", req.getUsername());
        assertEquals("user@test.com", req.getEmail());
        assertEquals("pass", req.getPassword());
        assertEquals("First", req.getFirstName());
        assertEquals("Last", req.getLastName());
    }
}
