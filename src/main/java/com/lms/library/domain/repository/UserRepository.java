package com.lms.library.domain.repository;

import com.lms.library.domain.entity.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
    Optional<User> findById(Long id);
    void deleteById(Long id);
}
