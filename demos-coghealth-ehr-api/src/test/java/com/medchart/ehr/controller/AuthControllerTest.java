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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private AuthController.SignUpRequest signUpRequest() {
        AuthController.SignUpRequest request = new AuthController.SignUpRequest();
        request.setUsername("new.provider");
        request.setEmail("new.provider@coghealth.test");
        request.setPassword("s3cret-pass");
        request.setFirstName("New");
        request.setLastName("Provider");
        return request;
    }

    @Test
    @SuppressWarnings("unchecked")
    void authenticateUser_returnsBearerTokenAndSetsSecurityContext() {
        Authentication authentication =
                new UsernamePasswordAuthenticationToken("dr.smith", "password");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token-value");

        ResponseEntity<?> response =
                authController.authenticateUser(loginRequest("dr.smith", "password"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body).containsEntry("token", "jwt-token-value");
        assertThat(body).containsEntry("type", "Bearer");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    void authenticateUser_propagatesBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() ->
                authController.authenticateUser(loginRequest("dr.smith", "wrong")))
                .isInstanceOf(BadCredentialsException.class);

        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void registerUser_persistsEncodedPasswordAndDefaultRole() {
        when(userRepository.existsByUsername("new.provider")).thenReturn(false);
        when(userRepository.existsByEmail("new.provider@coghealth.test")).thenReturn(false);
        when(passwordEncoder.encode("s3cret-pass")).thenReturn("encoded-secret");

        ResponseEntity<?> response = authController.registerUser(signUpRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("User registered successfully");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("new.provider");
        assertThat(saved.getPassword()).isEqualTo("encoded-secret");
        assertThat(saved.getRoles()).containsExactly(User.Role.PROVIDER);
        assertThat(saved.getEnabled()).isTrue();
    }

    @Test
    void registerUser_rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("new.provider")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Username is already taken!");
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_rejectsDuplicateEmail() {
        when(userRepository.existsByUsername("new.provider")).thenReturn(false);
        when(userRepository.existsByEmail("new.provider@coghealth.test")).thenReturn(true);

        ResponseEntity<?> response = authController.registerUser(signUpRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Email is already in use!");
        verify(userRepository, never()).save(any());
    }
}
