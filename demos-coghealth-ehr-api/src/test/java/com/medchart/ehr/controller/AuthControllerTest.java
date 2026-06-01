package com.medchart.ehr.controller;

import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @InjectMocks
    private AuthController authController;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private AuthController.LoginRequest loginRequest(String username, String password) {
        AuthController.LoginRequest req = new AuthController.LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        return req;
    }

    private AuthController.SignUpRequest signUpRequest() {
        AuthController.SignUpRequest req = new AuthController.SignUpRequest();
        req.setUsername("newuser");
        req.setEmail("new@example.com");
        req.setPassword("plain-password");
        req.setFirstName("New");
        req.setLastName("User");
        return req;
    }

    @Test
    @SuppressWarnings("unchecked")
    void authenticateUser_returnsBearerTokenAndSetsSecurityContext() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken("alice", "secret");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("signed-jwt");

        ResponseEntity<?> response = authController.authenticateUser(loginRequest("alice", "secret"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("token", "signed-jwt");
        assertThat(body).containsEntry("type", "Bearer");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);
    }

    @Test
    @SuppressWarnings("unchecked")
    void authenticateUser_passesCredentialsToAuthenticationManager() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("bob", "pw"));
        when(tokenProvider.generateToken(any())).thenReturn("jwt");

        authController.authenticateUser(loginRequest("bob", "pw"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("bob");
        assertThat(captor.getValue().getCredentials()).isEqualTo("pw");
    }

    @Test
    void registerUser_persistsUserWithEncodedPasswordAndDefaultRole() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        ResponseEntity<?> response = authController.registerUser(signUpRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("User registered successfully");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("newuser");
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        assertThat(saved.getPassword()).isEqualTo("encoded-password");
        assertThat(saved.getRoles()).containsExactly(User.Role.PROVIDER);
        assertThat(saved.getEnabled()).isTrue();
        assertThat(saved.getAccountNonExpired()).isTrue();
        assertThat(saved.getAccountNonLocked()).isTrue();
        assertThat(saved.getCredentialsNonExpired()).isTrue();
    }

    @Test
    void registerUser_rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Username is already taken!");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_rejectsDuplicateEmail() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Email is already in use!");
        verify(userRepository, never()).save(any());
    }
}
