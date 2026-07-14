package com.medchart.ehr.controller;

import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management endpoints (ADMIN only)")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List all users")
    public List<UserResponse> getAll() {
        return userService.findAll().stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update a user's roles")
    public ResponseEntity<UserResponse> updateRoles(@PathVariable Long id,
                                                    @RequestBody UpdateRolesRequest request) {
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return userService.updateRoles(id, request.getRoles())
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Enable or disable a user")
    public ResponseEntity<UserResponse> setStatus(@PathVariable Long id,
                                                  @RequestBody UpdateStatusRequest request) {
        if (request.getEnabled() == null) {
            return ResponseEntity.badRequest().build();
        }
        return userService.setEnabled(id, request.getEnabled())
                .map(UserResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return userService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Set<User.Role> roles;
        private Boolean enabled;

        public static UserResponse from(User user) {
            UserResponse response = new UserResponse();
            response.id = user.getId();
            response.username = user.getUsername();
            response.email = user.getEmail();
            response.firstName = user.getFirstName();
            response.lastName = user.getLastName();
            response.roles = user.getRoles() == null ? null : new HashSet<>(user.getRoles());
            response.enabled = user.getEnabled();
            return response;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public Set<User.Role> getRoles() { return roles; }
        public Boolean getEnabled() { return enabled; }
    }

    public static class UpdateRolesRequest {
        private Set<User.Role> roles;
        public Set<User.Role> getRoles() { return roles; }
        public void setRoles(Set<User.Role> roles) { this.roles = roles; }
    }

    public static class UpdateStatusRequest {
        private Boolean enabled;
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }
}
