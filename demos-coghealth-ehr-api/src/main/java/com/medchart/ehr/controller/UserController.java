package com.medchart.ehr.controller;

import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.dto.UserDTO;
import com.medchart.ehr.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User management endpoints. Access is restricted to authenticated administrators
 * via the security configuration ({@code /api/users/**} requires ROLE_ADMIN).
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getAll() {
        return userService.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(UserDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UserDTO> updateRoles(@PathVariable Long id,
                                               @RequestBody UpdateRolesRequest request,
                                               @AuthenticationPrincipal User currentUser) {
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (isSelf(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return userService.updateRoles(id, request.getRoles())
                .map(UserDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<UserDTO> updateStatus(@PathVariable Long id,
                                                @RequestBody UpdateStatusRequest request,
                                                @AuthenticationPrincipal User currentUser) {
        if (request.getEnabled() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (isSelf(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return userService.updateEnabled(id, request.getEnabled())
                .map(UserDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        if (isSelf(currentUser, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return userService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /** Guards against an admin modifying or removing their own account (which could lock everyone out). */
    private boolean isSelf(User currentUser, Long targetId) {
        return currentUser != null && targetId.equals(currentUser.getId());
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
