package com.medchart.ehr.controller;

import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final SessionService sessionService;

    @Value("${medchart.security.jwt.expiration}")
    private long accessTokenExpirationMs;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        SessionService.IssuedSession session = sessionService.startSession(loginRequest.getUsername());
        String jwt = tokenProvider.generateToken(authentication, session.getSessionId());

        return ResponseEntity.ok(tokenResponse(jwt, session.getRefreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshRequest refreshRequest) {
        Optional<SessionService.IssuedSession> rotated = sessionService.rotate(refreshRequest.getRefreshToken());

        if (!rotated.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired or revoked");
        }

        SessionService.IssuedSession session = rotated.get();
        String username = sessionService.findActive(session.getSessionId())
            .map(s -> s.getUsername())
            .orElseThrow(() -> new IllegalStateException("Rotated session is not active"));
        String jwt = tokenProvider.generateTokenFromUsername(username, session.getSessionId());

        return ResponseEntity.ok(tokenResponse(jwt, session.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String sessionId = tokenProvider.getSessionIdFromToken(authorizationHeader.substring(7));
            if (sessionId != null) {
                sessionService.revoke(sessionId);
            }
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    private Map<String, String> tokenResponse(String jwt, String refreshToken) {
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("refreshToken", refreshToken);
        response.put("expiresIn", String.valueOf(accessTokenExpirationMs / 1000));
        return response;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                .body("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body("Email is already in use!");
        }

        // Create user's account
        User user = User.builder()
            .username(signUpRequest.getUsername())
            .email(signUpRequest.getEmail())
            .firstName(signUpRequest.getFirstName())
            .lastName(signUpRequest.getLastName())
            .password(passwordEncoder.encode(signUpRequest.getPassword()))
            .roles(Set.of(User.Role.PROVIDER)) // Default role
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .build();

        userRepository.save(user);

        log.info("Registered new user: {}", signUpRequest.getUsername());
        
        return ResponseEntity.ok("User registered successfully");
    }

    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RefreshRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class SignUpRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }
}