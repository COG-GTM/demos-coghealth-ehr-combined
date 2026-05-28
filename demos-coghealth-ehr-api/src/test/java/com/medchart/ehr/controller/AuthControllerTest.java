package com.medchart.ehr.controller;

import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    // --- Login tests ---

    @Test
    void login_returnsTokenOnValidCredentials() {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("dr.smith");
        loginRequest.setPassword("password123");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token-value");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("jwt-token-value", body.get("token"));
        assertEquals("Bearer", body.get("type"));
    }

    @Test
    void login_throwsOnInvalidCredentials() {
        AuthController.LoginRequest loginRequest = new AuthController.LoginRequest();
        loginRequest.setUsername("dr.smith");
        loginRequest.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authController.authenticateUser(loginRequest));
    }

    // --- Registration tests ---

    @Test
    void register_createsUserOnValidRequest() {
        AuthController.SignUpRequest signUpRequest = createSignUpRequest(
                "newuser", "new@medchart.com", "pass123", "Jane", "Doe");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass");

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_rejectsDuplicateUsername() {
        AuthController.SignUpRequest signUpRequest = createSignUpRequest(
                "existing", "new@medchart.com", "pass123", "Jane", "Doe");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is already taken!", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_rejectsDuplicateEmail() {
        AuthController.SignUpRequest signUpRequest = createSignUpRequest(
                "newuser", "existing@medchart.com", "pass123", "Jane", "Doe");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@medchart.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already in use!", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_encodesPasswordBeforeSaving() {
        AuthController.SignUpRequest signUpRequest = createSignUpRequest(
                "newuser", "new@medchart.com", "plaintext", "Jane", "Doe");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode("plaintext")).thenReturn("$2a$encoded");

        authController.registerUser(signUpRequest);

        verify(passwordEncoder).encode("plaintext");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("$2a$encoded")));
    }

    @Test
    void register_assignsDefaultProviderRole() {
        AuthController.SignUpRequest signUpRequest = createSignUpRequest(
                "newuser", "new@medchart.com", "pass123", "Jane", "Doe");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        authController.registerUser(signUpRequest);

        verify(userRepository).save(argThat(user ->
                user.getRoles().contains(User.Role.PROVIDER) && user.getRoles().size() == 1));
    }

    @Test
    void register_setsAccountFlagsToTrue() {
        AuthController.SignUpRequest signUpRequest = createSignUpRequest(
                "newuser", "new@medchart.com", "pass123", "Jane", "Doe");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@medchart.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        authController.registerUser(signUpRequest);

        verify(userRepository).save(argThat(user ->
                user.getEnabled() && user.getAccountNonExpired()
                        && user.getAccountNonLocked() && user.getCredentialsNonExpired()));
    }

    // --- LoginRequest / SignUpRequest DTO tests ---

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
        req.setEmail("user@example.com");
        req.setPassword("pass");
        req.setFirstName("First");
        req.setLastName("Last");

        assertEquals("user", req.getUsername());
        assertEquals("user@example.com", req.getEmail());
        assertEquals("pass", req.getPassword());
        assertEquals("First", req.getFirstName());
        assertEquals("Last", req.getLastName());
    }

    private AuthController.SignUpRequest createSignUpRequest(
            String username, String email, String password, String firstName, String lastName) {
        AuthController.SignUpRequest req = new AuthController.SignUpRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);
        req.setFirstName(firstName);
        req.setLastName(lastName);
        return req;
    }
}
