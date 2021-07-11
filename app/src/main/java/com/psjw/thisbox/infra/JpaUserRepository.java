package com.psjw.thisbox.infra;

import com.psjw.thisbox.domain.User;
import com.psjw.thisbox.domain.UserRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface JpaUserRepository extends CrudRepository<User, Long>, UserRepository {
    Optional<User> findByUserId(Long userId);

    Optional<User> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String userEmail);

    List<User> findAll();

    User save(User user);

    Optional<User> findByUserIdAndDeletedIsFalse(Long userId);
}
