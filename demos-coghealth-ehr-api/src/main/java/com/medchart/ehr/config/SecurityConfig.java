package com.medchart.ehr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MaxLengthBCryptPasswordEncoder();
    }

    /**
     * BCrypt only considers the first 72 bytes of a password. Older
     * spring-security-crypto (CVE-2025-22228) silently ignores the excess, so two
     * passwords sharing their first 72 bytes are treated as equal. This encoder
     * rejects raw passwords longer than 72 bytes on both encode and match, matching
     * the behaviour of the patched BCryptPasswordEncoder.
     */
    static final class MaxLengthBCryptPasswordEncoder implements PasswordEncoder {

        private static final int MAX_BYTE_LENGTH = 72;

        private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

        @Override
        public String encode(CharSequence rawPassword) {
            checkLength(rawPassword);
            return delegate.encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            if (rawPassword != null
                    && rawPassword.toString().getBytes(StandardCharsets.UTF_8).length > MAX_BYTE_LENGTH) {
                return false;
            }
            return delegate.matches(rawPassword, encodedPassword);
        }

        private static void checkLength(CharSequence rawPassword) {
            if (rawPassword != null
                    && rawPassword.toString().getBytes(StandardCharsets.UTF_8).length > MAX_BYTE_LENGTH) {
                throw new IllegalArgumentException("Password cannot be more than " + MAX_BYTE_LENGTH + " bytes");
            }
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/**").permitAll();
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*", "http://10.*:*", "http://192.168.*:*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
