package com.ihsanerben.ecommerce_simulation_api.auth.repository;

import com.ihsanerben.ecommerce_simulation_api.auth.entity.User;
import com.ihsanerben.ecommerce_simulation_api.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findAllByRole(Role role);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
