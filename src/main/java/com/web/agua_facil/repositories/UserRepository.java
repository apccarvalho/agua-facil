package com.web.agua_facil.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.web.agua_facil.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}