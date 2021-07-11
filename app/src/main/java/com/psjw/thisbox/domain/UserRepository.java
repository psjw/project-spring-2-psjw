package com.psjw.thisbox.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUserId(Long userId);

    Optional<User> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String userEmail);

    List<User> findAll();

    User save(User user);

    Optional<User> findByUserIdAndDeletedIsFalse(Long userId);
}
