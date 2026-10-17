package com.medchart.ehr.service;

import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public Optional<User> updateRoles(Long id, Set<User.Role> roles) {
        return userRepository.findById(id).map(user -> {
            user.setRoles(roles);
            User saved = userRepository.save(user);
            log.info("Updated roles for user {}: {}", user.getUsername(), roles);
            return saved;
        });
    }

    @Transactional
    public Optional<User> setEnabled(Long id, boolean enabled) {
        return userRepository.findById(id).map(user -> {
            user.setEnabled(enabled);
            User saved = userRepository.save(user);
            log.info("Set enabled={} for user {}", enabled, user.getUsername());
            return saved;
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        log.info("Deleted user with id {}", id);
        return true;
    }
}
